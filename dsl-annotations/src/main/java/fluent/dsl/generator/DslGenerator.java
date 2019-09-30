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

import fluent.dsl.model.*;

import java.io.PrintWriter;
import java.io.Writer;

import static java.util.stream.Collectors.joining;

public class DslGenerator {

    private static final String tab = "\t";

    private final PrintWriter source;
    private final String prefix;

    private DslGenerator(PrintWriter source, String prefix) {
        this.source = source;
        this.prefix = prefix;
    }

    private DslGenerator indent() {
        return new DslGenerator(source, prefix + tab);
    }

    private void println(String line) {
        source.println(prefix + line);
    }

    private void println() {
        source.println();
    }

    public static void generateFrom(Writer writer, DslModel model) {
        try(PrintWriter source = new PrintWriter(writer)) {
            new DslGenerator(source, "").generateDsl(model);
        }
    }

    private void generateDsl(DslModel model) {
        println("package " + model.packageName() + ";");
        println();
        println("import fluent.api.Start;");
        println("import fluent.api.End;");
        println();
        println();
        println("public interface " + model.name() + "{");
        println();
        indent().generateMethod("static", model.factory());
        println();
        indent().generateInterfaceContent(model);
        println();
        indent().generateDelegate(model);
        println();
        println("}");
    }

    private void generateDelegate(DslModel model) {
        println("public interface Delegate extends " + model.name() + " {");
        DslGenerator indent = indent();
        indent.generateSignature(model.delegate());
        model.keywords().forEach(keyword -> indent.generateDelegateMethod(keyword, model.delegate()));
        println("}");
    }

    private void generateDelegateMethod(KeywordModel model, KeywordModel delegate) {
        println("default " + model.type() + " " + model.name() + "(" + parameters(model) + ") {");
        indent().println(returnType(model) + delegate.name() + "()." + model.name() + "(" + args(model) + ");");
        println("}");
    }

    private void generateInterfaceContent(TypeModel model) {
        model.keywords().forEach(this::generateSignature);
        model.keywords().stream().filter(kw -> !kw.hasBinding()).map(KeywordModel::type).forEach(this::generateInterface);
    }

    private void generateInterface(TypeModel model) {
        println();
        println("public interface " + model.name() + " {");
        indent().generateInterfaceContent(model);
        println("}");
    }

    private void generateSignature(KeywordModel model) {
        println((model.hasBinding() ? "@End " : "@Start(\"Unterminated sentence.\") ") + model.type() + " " + model.name() + "(" + parameters(model) + ");");
        model.aliases().forEach(alias -> {
            println(model.type() + " " + alias + "(" + parameters(model) + ");");
            indent().println(returnType(model) + model.name() + "(" + args(model) + ");");
            println("}");
        });
    }

    private String returnType(KeywordModel model) {
        return "void".equals(model.type().name()) ? "" : "return ";
    }

    private void generateMethod(String modifiers, KeywordModel model) {
        println(modifiers + " " + model.type().name() + " " + model.name() + "(" + parameters(model) + ") {");
        if(model.hasBinding()) {
            BindingModel binding = model.binding();
            indent().println(returnType(binding.method()) + binding.target().name() + "." + binding.method().name() + "(" + args(binding.method()) + ");");
        } else {
            indent().generateReturnAnonymousClass(model.type());
        }
        println("}");
    }

    private void generateReturnAnonymousClass(TypeModel model) {
        println("return new " + model.name() + "() {");
        DslGenerator indent = indent();
        model.keywords().forEach(keywordModel -> indent.generateMethod("public", keywordModel));
        println("};");
    }

    private String parameters(KeywordModel model) {
        return model.parameters().stream().map(p -> p.type().name() + " " + p.name()).collect(joining(", "));
    }

    private String args(KeywordModel model) {
        return model.parameters().stream().map(BaseModel::name).collect(joining(", "));
    }

}
