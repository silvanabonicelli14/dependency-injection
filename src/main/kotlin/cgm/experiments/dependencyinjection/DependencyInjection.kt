package cgm.experiments.dependencyinjection
import cgm.experiments.dependencyinjection.DependencyInjection.add
import cgm.experiments.dependencyinjection.DependencyInjection.addI
import cgm.experiments.dependencyinjection.DependencyInjection.container
import cgm.experiments.dependencyinjection.annotation.Injected
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

object DependencyInjection {

    var container =  mutableMapOf<KClass<*>,KClass<*>>()
    var containerFunctions: MutableMap<KClass<Any>, DependencyInjection.() -> Any> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: Any> add() {
        add(T::class)
    }

    fun <T: Any> add(clazz: KClass<T>) {
        container[clazz] = clazz as KClass<*>
    }

    inline fun <reified T: Any> get(): T? {
        return get(T::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> get(clazz: KClass<T>): T? {

        val constructors =
            container[clazz]?.constructors
            ?: return containerFunctions[clazz as KClass<Any>]?.invoke(this) as T?

        val emptyConstructor = constructors.firstOrNull { it.parameters.isEmpty() }
        val result =
            emptyConstructor?.call()
            ?: callConstructorWithArgs(constructors)

        return result as T?
    }

    private fun callConstructorWithArgs(constructors: Collection<KFunction<Any>>): Any {
        val constructor = constructors.minByOrNull { it.parameters.size }!!

        val args = constructor.parameters.map {
            get(it.type.jvmErasure)
        }.toTypedArray()

        return constructor.call(*args)
    }

    inline fun <reified T: Any, reified U: T> addI() {
        addI(T::class,U::class)
    }

    fun <T: Any, U: T> addI(interfaze: KClass<T>, clazz: KClass<U>) {
        container[interfaze] = clazz
    }

    inline fun <reified T: Any> add(noinline factoryFn: DependencyInjection.() -> T) {
        add(T::class, factoryFn)
    }

    fun <T: Any> add(clazz: KClass<T>, factoryFn: DependencyInjection.() -> T) {
        containerFunctions[clazz as KClass<Any>] = factoryFn
    }

    fun reset() {
        container.clear()
        containerFunctions.clear()
    }
}

inline fun <reified T> di(function: DependencyInjection.() -> T) = DependencyInjection.function()

fun diAutoConfigure(packageName: String) {
    Reflections(packageName)
        .getTypesAnnotatedWith(Injected::class.java)
        .forEach {
            val clazz = Class.forName(it.name).kotlin as KClass<*>
            clazz.supertypes.forEach { supertype ->
                val classInterface = supertype.classifier as KClass<*>
                if (classInterface.qualifiedName?.contains(packageName) == true){
                    when {
                        classInterface.isAbstract -> {container[classInterface] = clazz}
                        else -> add(clazz)
                    }
                }
            }
    }
}
