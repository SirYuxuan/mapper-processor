package com.yuxuan66.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import com.google.auto.service.AutoService;
import com.yuxuan66.annotation.Mapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Sir丶雨轩
 * @since 2021/6/24
 */
@AutoService(Processor.class)
public class DaoProcessor extends AbstractProcessor {

    private Types mTypeUtils;
    private Elements mElementUtils;
    private Filer mFiler;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        //把我们自己定义的注解添加进去
        annotations.add(Mapper.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        mTypeUtils = processingEnv.getTypeUtils();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }
    private void error(Element e, String msg, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Mapper.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "Only classes can be annotated with @%s", Mapper.class.getSimpleName());
                return true;
            }
            // //解析，并生成代码
            try {

                analysisAnnotated(annotatedElement);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void analysisAnnotated(Element classElement) throws IOException {

        Mapper annotation = classElement.getAnnotation(Mapper.class);

        InputStream inputStream = this.getClass().getResourceAsStream("/template/Dao.btl");

        String daoContent = IoUtil.read(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);

        String packageName = mElementUtils.getPackageOf(classElement).getQualifiedName().toString();


        daoContent = daoContent.replaceAll("#date", DateUtil.format(new Date(),"yyyy/MM/dd"));
        daoContent = daoContent.replaceAll("#package",packageName);
        daoContent = daoContent.replaceAll("#className",classElement.getSimpleName().toString());

        try {
            JavaFileObject source = mFiler.createSourceFile(packageName + "." + classElement.getSimpleName() + "Mapper");
            Writer writer = source.openWriter();
            writer.write(daoContent);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
    }
}
