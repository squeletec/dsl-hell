/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2019, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package fluent.dsl.generator;

import fluent.api.model.MethodModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fluent.dsl.model.DslUtils.generic;
import static java.util.stream.Collectors.joining;

/**
 * Generator of the DSL, operating on model of classes, making it up.
 * Whole DSL is now generated as one top level interface containing nested interfaces of all possible chaining.
 * Subsequent chain is always again nested, so that easy scoping prevents conflits across DSL branches.
 *
 * As the approach is heavily recursive and needs indentation etc. it's not a good fit for any template usage.
 */
public class DslGenerator {

    private static final String tab = "\t";

    private final PrintWriter source;
    private final String prefix;
    private final boolean useVarargs;
    private final Set<TypeModel> generated = new HashSet<>();

    private DslGenerator(PrintWriter source, String prefix, boolean useVarargs) {
        this.source = source;
        this.prefix = prefix;
        this.useVarargs = useVarargs;
    }

    private DslGenerator indent() {
        return new DslGenerator(source, prefix + tab, useVarargs);
    }

    private void println(String line) {
        source.println(prefix + line);
    }

    private void println() {
        source.println();
    }

    public static void generateFrom(Writer writer, boolean useVarargs, Consumer<DslGenerator> consumer) {
        try(PrintWriter source = new PrintWriter(writer)) {
            consumer.accept(new DslGenerator(source, "", useVarargs));
        }
    }

    public void generateDsl(TypeModel delegateModel) {
        DslGenerator nested = indent();
        TypeModel model = delegateModel.superClass();
        println("package " + model.packageName() + ";");
        println();
        println("import fluent.api.Start;");
        println("import fluent.api.End;");
        println();
        println();
        println("public interface " + model.simpleName() + "{");
        model.fields().forEach(nested::generateConstant);
        println();
        nested.generateInterfaceContent(model);
        println();
        nested.generateDelegate(delegateModel);
        println();
        model.fields().forEach(nested::generateConstantClass);
        println("}");
    }

    public void generateBuilder(TypeModel model) {
        DslGenerator nested = indent();
        println("package " + model.packageName() + ";");
        println();
        println("import fluent.api.Start;");
        println("import fluent.api.End;");
        println();
        println();
        println("public interface " + model.simpleName() + "{");
        println();
        nested.generateInterfaceContent(model);
        println();
        //nested.generateDelegate(model);
        println();
        println("}");
    }

    private void generateConstant(VarModel constant) {
        println("public static final " + constant.type().simpleName() + " " + constant.name() + " = new " + constant.type().simpleName() + "();");
    }

    private void generateConstantClass(VarModel constant) {
        println();
        println("public static final class " + constant.type().simpleName() + "{");
        indent().println("private " + constant.type().simpleName() + "() {}");
        println("}");
    }

    private void generateDelegate(TypeModel delegateModel) {
        TypeModel model = delegateModel.superClass();
        println("public interface Delegate" + generic(model) + " extends " + model.simpleName() + " {");
        DslGenerator indent = indent();
        indent.generateSignature(delegateModel.methods().get(0));
        model.methods().stream().filter(m -> !m.modifiers().isStatic()).forEach(keyword -> indent.generateDelegateMethod(keyword, delegateModel.methods().get(0)));
        println("}");
    }

    private void generateDelegateMethod(MethodModel model, MethodModel delegate) {
        println("default " + model.returnType().fullName() + " " + model.name() + "(" + parameters(model) + ") {");
        indent().println(returnType(model) + delegate.name() + "()." + model.name() + "(" + args(model) + ");");
        println("}");
    }

    private void generateInterfaceContent(TypeModel model) {
        generated.add(model);
        model.methods().forEach(this::generateSignature);
        model.methods().stream().filter(kw -> kw.body().isEmpty()).map(MethodModel::returnType).filter(t -> !generated.contains(t)).forEach(this::generateInterface);
    }

    private void generateInterface(TypeModel model) {
        println();
        println("public interface " + model.simpleName() + " {");
        indent().generateInterfaceContent(model);
        println("}");
    }

    private void generateSignature(MethodModel model) {
        if(model.modifiers().isStatic())
            generateMethod(model);
        else {
            println((model.body().isEmpty() ? "@Start(\"Unterminated sentence.\") " : "@End ") + generic(model) + " " + model.returnType().fullName() + " " + model.name() + "(" + parameters(model) + ");");
            ((List<String>)model.metadata().getOrDefault("aliases", Collections.emptyList())).forEach(alias -> {
                println("default " + generic(model) + " " + model.returnType().fullName() + " " + alias + "(" + parameters(model) + ") {");
                indent().println(returnType(model) + model.name() + "(" + args(model) + ");");
                println("}");
            });
        }
    }

    private String returnType(MethodModel model) {
        return model.returnsValue() ? "return " : "";
    }

    private void generateMethod(MethodModel model) {
        println("public " + (model.modifiers().isStatic() ? "static " : "") + generic(model) + " " + model.returnType().fullName() + " " + model.name() + "(" + parameters(model) + ") {");
        DslGenerator nested = indent();
        if(model.body().isEmpty()) {
            nested.generateReturnAnonymousClass(model.returnType());
        } else {
            model.body().forEach(statementModel -> nested.println(statementModel.toString()));
        }
        println("}");
    }

    private void generateReturnAnonymousClass(TypeModel model) {
        println("return new " + model.simpleName() + "() {");
        DslGenerator indent = indent();
        model.methods().stream().filter(m -> !m.modifiers().isStatic()).forEach(indent::generateMethod);
        println("};");
    }

    private String parameters(MethodModel model) {
        List<String> collect = model.parameters().stream().map(p -> p.type().fullName() + " " + p.name()).collect(Collectors.toList());
        if(useVarargs && !collect.isEmpty()) {
            collect.set(collect.size() - 1, collect.get(collect.size() - 1).replace("[] ", "... "));
        }
        return String.join(", ", collect);
    }

    private String args(MethodModel model) {
        return model.parameters().stream().map(VarModel::name).collect(joining(", "));
    }

}
