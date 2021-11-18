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

import fluent.api.model.*;

import javax.lang.model.element.Modifier;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public class DslWriter {

    private static final String tab = "\t";
    private static final Set<String> forbidden = new HashSet<>(asList("toString()", "hashCode()", "getClass()"));

    private final PrintWriter source;
    private final String prefix;

    private DslWriter(PrintWriter source, String prefix) {
        this.source = source;
        this.prefix = prefix;
    }

    public static DslWriter dslWriter(PrintWriter printWriter) {
        return new DslWriter(printWriter, "");
    }

    private DslWriter indent() {
        return new DslWriter(source, prefix + tab);
    }

    private void println() {
        source.println();
    }

    private void println(String line) {
        source.println(prefix + line);
    }

    private void print(String line) {
        source.print(line);
    }
    private void println(String template, String... args) {
        println(format(template, (Object[]) args));
    }

    public void writeFile(TypeModel<?> model) {
        println("package %s;", model.packageName());
        println();
        println("import javax.annotation.Generated;");
        println("import fluent.api.Start;");
        println("import fluent.api.End;");
        println();
        println("@Generated(\"Generated DSL class\")");
        print("public ");
        writeType(model);
    }

    public void writeType(TypeModel<?> model) {
        if(model instanceof InterfaceModel)
            writeInterface((InterfaceModel) model);
        if(model instanceof ClassModel)
            writeClass((ClassModel) model);
    }

    public void writeInterface(InterfaceModel model) {
        println("interface %s%s {", model.simpleName(), interfaces("extends", model.interfaces()));
        DslWriter indent = indent();
        model.fields().values().forEach(indent::writeField);
        model.methods().forEach(indent::writeInterfaceMethod);
        model.types().forEach(indent::writeType);
        println("}");
    }

    private void writeInterfaceMethod(MethodModel model) {
        if(model instanceof StaticMethodModel)
            writeStaticMethod((StaticMethodModel) model);
        else if(model instanceof DefaultMethodModel)
            writeDefaultMethod((DefaultMethodModel) model);
        else
            writeSignature(model);
    }

    private String interfaces(String keyword, List<InterfaceModel> interfaces) {
        return interfaces.isEmpty() ? "" : interfaces.stream().map(TypeModel::fullName).collect(joining(", ", " " + keyword + " ", ""));
    }

    public String extend(ClassModel superClass) {
        return isNull(superClass) ? "" : " extends " + superClass.fullName();
    }

    public void writeField(VarModel model) {
        println("%s%s %s%s;", modifiers(model), model.type().fullName(), model.name(), isNull(model.initializer())? "" : " = " + model.initializer());
    }

    private String modifiers(ElementModel model) {
        return model.modifiers().keywords().isEmpty() ? "" : model.modifiers().keywords().stream().map(Modifier::toString).collect(joining(" ")) + " ";
    }

    public void writeClass(ClassModel model) {
        println("public class %s%s%s {", model.simpleName(), extend(model.superClass()), interfaces("implements", model.interfaces()));
        DslWriter indent = indent();
        model.fields().values().forEach(indent::writeField);
        model.methods().forEach(indent::writeClassMethod);
        model.types().forEach(indent::writeType);
        println("}");
    }

    private String typeParameters(GenericModel<?> model) {
        return model.typeParameters().isEmpty() ? "" : model.typeParameters().stream().map(TypeModel::simpleName).collect(joining(", ", "<", "> "));
    }

    private String annotations(ElementModel model) {
        return model.annotations().stream().map(a -> "@" + a.type().simpleName()).collect(joining());
    }

    public void writeSignature(MethodModel model) {
        println("%s%s %s(%s);", typeParameters(model), model.returnType().fullName(), model.name(), parameters(model));
    }

    public void writeClassMethod(MethodModel model) {
        if(model instanceof ConstructorModel)
            writeConstructor((ConstructorModel) model);
        else if(model instanceof StaticMethodModel)
            writeStaticMethod((StaticMethodModel) model);
        else
            writeMethod(model);
    }

    public void writeMethod(MethodModel model) {
        println("public %s%s %s(%s) {", typeParameters(model), model.returnType().fullName(), model.name(), parameters(model));
        model.body().forEach(indent()::writeStatement);
        println("}");
    }

    private void writeStatement(StatementModel statementModel) {
        println(statementModel.toString());
    }

    private String parameters(MethodModel model) {
        return model.parameters().stream().map(p -> p.type().fullName() + " " + p.name()).collect(joining(", "));
    }

    public void writeConstructor(ConstructorModel model) {
        println("%s %s(%s) {", modifiers(model), model.returnType().rawType().simpleName(), parameters(model));
        model.body().forEach(indent()::writeStatement);
        println("}");
    }

    public void writeStaticMethod(StaticMethodModel model) {
        writeMethod("public static", model);
    }

    public void writeAnonymousClass(InterfaceModel model) {
        println("return new %s() {", model.fullName());
        model.methods().stream().filter(m -> !(m instanceof StaticMethodModel) && !(m instanceof DefaultMethodModel)).forEach(indent()::writeAnonymousImplementation);
        println("};");
    }

    public void writeAnonymousImplementation(MethodModel model) {
        writeMethod("public", model);
    }

    public void writeDefaultMethod(DefaultMethodModel model) {
        if(!forbidden.contains(model.toString()))
            writeMethod("default", model);
    }

    private void writeMethod(String prefix, MethodModel model) {
        println("%s %s%s%s %s(%s) {", prefix, annotations(model), typeParameters(model), model.returnType().fullName(), model.name(), parameters(model));
        if(model.body().isEmpty() && model.returnType() instanceof InterfaceModel)
            indent().writeAnonymousClass((InterfaceModel) model.returnType());
        model.body().forEach(indent()::writeStatement);
        println("}");
    }

}
