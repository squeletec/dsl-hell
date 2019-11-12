package fluent.api.model.impl;

import fluent.api.model.ClassModel;
import fluent.api.model.ModifiersModel;
import fluent.api.model.TypeModel;

import javax.lang.model.type.TypeKind;
import java.util.List;

public class ClassModelImpl extends TypeModelImpl<ClassModel> implements ClassModel {

    private ClassModel superClass;

    public ClassModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind) {
        super(modifiers, packageName, simpleName, fullName, kind);
    }

    public ClassModelImpl(ModifiersModel modifiers, String packageName, String simpleName, String fullName, TypeKind kind, List<TypeModel> typeParameters, ClassModel rawType) {
        super(modifiers, packageName, simpleName, fullName, kind, typeParameters, rawType);
    }

    @Override
    protected ClassModel t() {
        return this;
    }

    @Override
    protected ClassModel construct(String collect, List<TypeModel> typeParameters) {
        return new ClassModelImpl(modifiers(), packageName(), simpleName() + collect, fullName() + collect, TypeKind.DECLARED, typeParameters, this);
    }

    @Override
    public ClassModel superClass(ClassModel superClass) {
        this.superClass = superClass;
        return this;
    }

    @Override
    public ClassModel superClass() {
        return superClass;
    }

}
