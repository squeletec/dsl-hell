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
package fluent.dsl.plugin;

import fluent.api.model.GenericModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;
import fluent.dsl.Dsl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.STATIC;

public final class DslUtils {

    public static String capitalize(String string) {
        return string.isEmpty() ? string : string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String unCapitalize(String string) {
        return string.isEmpty() ? string : string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    public static String simpleName(TypeModel model) {
        return model.isArray() ? simpleName(model.componentType()) + "Array" : model.rawType().simpleName();
    }

    public static String generic(GenericModel<?> model) {
        return model.typeParameters().isEmpty() ? "" : model.typeParameters().stream().map(TypeModel::fullName).collect(joining(", ", "<", ">"));
    }

    public static void traverse(TypeModel<?> t, List<TypeModel> out) {
        if(t.isTypeVariable())
            out.add(t);
        else
            t.typeParameters().forEach(p -> traverse(p, out));
    }

    public static List<TypeModel> usedTypeParameters(List<VarModel> parameters) {
        List<TypeModel> out = new ArrayList<>();
        parameters.stream().map(VarModel::type).forEach(t -> traverse(t, out));
        return out;
    }

    public static String override(String configuredValue, String defaultValue) {
        return configuredValue.isEmpty() ? defaultValue : configuredValue;
    }


    public static boolean isSetter(ExecutableElement method) {
        return !method.getModifiers().contains(STATIC) && method.getSimpleName().toString().startsWith("set") && method.getParameters().size() == 1;
    }

    public static boolean isGetter(ExecutableElement method) {
        return !method.getModifiers().contains(STATIC) && method.getSimpleName().toString().startsWith("get") && method.getParameters().size() == 0;
    }

    public static Dsl getDsl(Element element) {
        Dsl dsl = element.getAnnotation(Dsl.class);
        if(nonNull(dsl))
            return dsl;
        Element enclosingElement = element.getEnclosingElement();
        if(nonNull(enclosingElement))
            return getDsl(enclosingElement);
        try {
            Element packageElement = (Element) element.getClass().getField("owner").get(element);
            if(nonNull(packageElement))
                return getDsl(packageElement);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String from(Element element) {
        return element.getSimpleName().toString();
    }

}
