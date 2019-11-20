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

import fluent.api.model.ModelFactory;
import fluent.api.model.TypeModel;
import fluent.api.model.impl.ModelFactoryImpl;
import fluent.dsl.Dsl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fluent.dsl.generator.DslWriter.dslWriter;
import static java.util.ServiceLoader.load;
import static javax.tools.Diagnostic.Kind.WARNING;

@SupportedAnnotationTypes("fluent.dsl.Dsl")
public class DslAnnotationProcessor extends AbstractProcessor {

    private final List<DslAnnotationProcessorPlugin> plugins = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        ModelFactory modelFactory = new ModelFactoryImpl(env.getElementUtils(), env.getTypeUtils());
        try {
            load(DslAnnotationProcessorPluginFactory.class, DslAnnotationProcessorPluginFactory.class.getClassLoader()).forEach(factory -> plugins.add(factory.createPlugin(modelFactory)));
        } catch (RuntimeException | Error e) {
            env.getMessager().printMessage(WARNING, "Unable to load plugin: " + e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(Element element : roundEnv.getElementsAnnotatedWith(Dsl.class))
            processElement(element);
        return true;
    }

    private void processElement(Element element) {
        Dsl dsl = element.getAnnotation(Dsl.class);
        try {
            for(DslAnnotationProcessorPlugin plugin : plugins)
                if(plugin.isFor(element))
                    applyPlugin(plugin, element, dsl);
        } catch (Throwable throwable) {
            processingEnv.getMessager().printMessage(WARNING, "Unable to generate DSL for " + element + ": " + throwable, element);
        }
    }

    private void applyPlugin(DslAnnotationProcessorPlugin plugin, Element element, Dsl dsl) {
        TypeModel<?> model = plugin.process(element, dsl);
        try(PrintWriter writer = new PrintWriter(processingEnv.getFiler().createSourceFile(model.rawType().fullName()).openWriter())) {
            dslWriter(writer).writeFile(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
