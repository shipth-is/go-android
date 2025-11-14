import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class LogInterceptorMethodVisitor(
    apiVersion: Int,
    originalVisitor: MethodVisitor,
    access: Int,
    name: String,
    descriptor: String,
) : AdviceAdapter(apiVersion, originalVisitor, access, name, descriptor) {

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        desc: String?,
        itf: Boolean,
    ) {
        if (shouldReplaceLogCall(owner, name)) {
            // Replace with YOUR adapter class
            mv.visitMethodInsn(
                INVOKESTATIC,
                "com/shipthis/go/LogInterceptor", // Your package
                name,
                desc,
                false,
            )
        } else {
            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }
    }

    private fun shouldReplaceLogCall(owner: String, name: String): Boolean {
        val logMethods = setOf("v", "d", "i", "w", "e", "wtf")
        return owner == "android/util/Log" && logMethods.contains(name)
    }
}

