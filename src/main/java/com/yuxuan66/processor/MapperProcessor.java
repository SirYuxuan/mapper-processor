package com.yuxuan66.processor;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import com.google.auto.service.AutoService;
import com.yuxuan66.annotation.GenerateMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MapperProcessor 生成Mapper注解处理器
 * 用于处理GenerateMapper注解的注解处理器
 * 通过该处理器，可以解析GenerateMapper注解，并生成相应的代码
 * 该处理器使用AutoService注解，自动注册为注解处理器
 * 注解处理器必须继承AbstractProcessor类
 * `@AutoService(Processor.class)` 注解用于自动注册处理器
 *
 * @author Sir丶雨轩
 * @since 2021/6/24
 */
@AutoService(Processor.class)
public class MapperProcessor extends AbstractProcessor {

    private Elements mElementUtils;
    private Filer mFiler;
    private Messager messager;

    /**
     * 获取支持的注解类型
     * 在这里添加我们自定义的GenerateMapper注解
     *
     * @return 支持的注解类型集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        // 把我们自己定义的注解添加进去
        annotations.add(GenerateMapper.class.getCanonicalName());
        return annotations;
    }

    /**
     * 获取支持的Java版本
     *
     * @return 支持的Java版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 初始化方法
     *
     * @param processingEnv 注解处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * 处理注解的方法
     *
     * @param annotations 注解集合
     * @param roundEnv    当前处理环境
     * @return 是否处理完成
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(GenerateMapper.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, GenerateMapper.class.getSimpleName());
                return true;
            }
            // 解析，并生成代码
            try {
                analysisAnnotated(annotatedElement);
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    /**
     * 解析注解并生成代码的方法
     *
     * @param classElement 带有GenerateMapper注解的类元素
     * @throws IOException IO异常
     */
    private void analysisAnnotated(Element classElement) throws IOException {
        // 获取GenerateMapper注解
        GenerateMapper annotation = classElement.getAnnotation(GenerateMapper.class);

        // 从资源路径中获取Mapper模板文件的输入流
        InputStream inputStream = this.getClass().getResourceAsStream("/template/Mapper.btl");

        // 使用IoUtil工具类读取输入流中的内容，并将其转换为字符串
        String mapperContent = IoUtil.read(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);

        // 获取当前类元素的包名
        String packageName = mElementUtils.getPackageOf(classElement).getQualifiedName().toString();

        // 将模板中的"#date"替换为当前日期
        mapperContent = mapperContent.replaceAll("#date", DateUtil.format(new Date(), "yyyy/MM/dd"));

        // 将模板中的"#package"替换为当前包名
        mapperContent = mapperContent.replaceAll("#package", packageName);

        // 遍历GenerateMapper注解中的autoImport数组，生成import语句，并拼接到autoImportResult中
        StringBuilder autoImportResult = new StringBuilder();
        for (String autoImport : annotation.autoImport()) {
            autoImportResult.append("import ").append(autoImport).append(";\n");
        }

        // 将当前类的全限定名拼接到autoImportResult中
        autoImportResult.append("import ").append(packageName).append(".").append(classElement.getSimpleName()).append(";\n");

        // 将模板中的"#autoImport"替换为生成的import语句
        mapperContent = mapperContent.replaceAll("#autoImport", autoImportResult.toString());

        try {
            // 创建要生成的Java源文件
            JavaFileObject source = mFiler.createSourceFile(packageName + "." + classElement.getSimpleName() + "Mapper");

            // 将生成的代码写入Java源文件中
            Writer writer = source.openWriter();
            writer.write(mapperContent);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
            // 异常处理
        }
    }

    /**
     * 错误处理方法
     *
     * @param e    出错的元素
     * @param args 错误信息参数
     */
    private void error(Element e, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("Only classes can be annotated with @%s", args), e);
    }
}