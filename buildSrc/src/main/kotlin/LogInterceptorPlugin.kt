import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import org.gradle.api.Plugin
import org.gradle.api.Project

class LogInterceptorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.withPlugin("com.android.application") {
            val androidComponents = project.extensions
                .getByType(ApplicationAndroidComponentsExtension::class.java)
            
            androidComponents.onVariants { variant ->
                variant.instrumentation.transformClassesWith(
                    LogInterceptorClassVisitorFactory::class.java,
                    InstrumentationScope.ALL, // This includes AAR dependencies!
                ) { }
                
                variant.instrumentation.setAsmFramesComputationMode(
                    FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
                )
            }
        }
    }
}

