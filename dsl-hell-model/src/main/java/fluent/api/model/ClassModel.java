package fluent.api.model;

public interface ClassModel extends TypeModel<ClassModel> {

    ClassModel superClass();

    TypeModel superClass(ClassModel dslType);

}
