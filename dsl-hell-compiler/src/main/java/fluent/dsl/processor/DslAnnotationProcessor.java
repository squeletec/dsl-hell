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
package fluent.dsl.processor;

import fluent.api.model.TypeModel;
import fluent.api.model.impl.ModelFactoryImpl;
import fluent.dsl.Dsl;
import fluent.dsl.parser.BuilderParser;
import fluent.dsl.parser.DslParser;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.HashSet;
import java.util.Set;

import static fluent.dsl.generator.DslGenerator.generateFrom;
import static java.util.Arrays.asList;
import static javax.lang.model.element.ElementKind.*;
import static javax.tools.Diagnostic.Kind.WARNING;

@SupportedAnnotationTypes("fluent.dsl.Dsl")
public class DslAnnotationProcessor extends AbstractProcessor {

    private final Set<ElementKind> modelTypes = new HashSet<>(asList(INTERFACE, CLASS, PARAMETER));

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ModelFactoryImpl modelFactory = new ModelFactoryImpl(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        for(Element element : roundEnv.getElementsAnnotatedWith(Dsl.class))
            switch (element.getKind()) {
                case INTERFACE:
                case CLASS:
                    processBinding(element, new DslParser(modelFactory));
                    break;
                case FIELD:
                case PARAMETER:
                    processBuilder(element, new BuilderParser(modelFactory));
                    break;
            }
        return true;
    }

    private void processBinding(Element element, DslParser factory) {
        try {
            TypeModel model = factory.parseModel(element);
            generateFrom(
                    processingEnv.getFiler().createSourceFile(model.superClass().rawType().fullName()).openWriter(),
                    element.getAnnotation(Dsl.class).useVarargs(),
                    generator -> generator.generateDsl(model)
            );
        } catch (Throwable throwable) {
            processingEnv.getMessager().printMessage(WARNING, "Unable to generate DSL for " + element + ": " + throwable, element);
        }
    }

    private void processBuilder(Element element, BuilderParser parser) {
        try {
            TypeModel model = parser.parseModel(element);
            generateFrom(
                    processingEnv.getFiler().createSourceFile(model.rawType().fullName()).openWriter(),
                    element.getAnnotation(Dsl.class).useVarargs(),
                    generator -> generator.generateBuilder(model)
            );
        } catch (Throwable throwable) {
            processingEnv.getMessager().printMessage(WARNING, "Unable to generate DSL for " + element + ": " + throwable, element);
        }
    }


}
