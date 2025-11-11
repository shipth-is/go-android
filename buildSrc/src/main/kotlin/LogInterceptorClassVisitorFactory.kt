import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

abstract class LogInterceptorClassVisitorFactory :
    AsmClassVisitorFactory<LogInterceptorClassVisitorFactory.Parameters> {

    interface Parameters : InstrumentationParameters

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor,
    ): ClassVisitor {
        val apiVersion = instrumentationContext.apiVersion.get()
        return object : ClassVisitor(apiVersion, nextClassVisitor) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?,
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return LogInterceptorMethodVisitor(
                    apiVersion,
                    mv,
                    access,
                    name ?: "",
                    descriptor ?: "",
                )
            }
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        // Don't instrument LogInterceptor itself to avoid recursion
        if (classData.className.startsWith("com/shipthis/go/LogInterceptor")) {
            return false
        }
        // Instrument all other classes including AAR dependencies
        return true
    }
}

