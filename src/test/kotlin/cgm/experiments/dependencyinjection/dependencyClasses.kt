package cgm.experiments.dependencyinjection

import cgm.experiments.dependencyinjection.annotation.Injected


interface IDependency
interface INested

@Injected
class Dependency: IDependency {
    override fun equals(other: Any?): Boolean = when (other) {
        is Dependency -> true
        else -> false
    }
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@Injected
class Dependency2 {
    override fun equals(other: Any?): Boolean = when (other) {
        is Dependency2 -> true
        else -> false
    }
    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@Injected
data class Dependent(private val dependency: Dependency)

@Injected
data class Dependent2(private val dependency2: Dependency2, private val dependency1: Dependency)

@Injected
data class DependentByNested(private val dependency: Dependent2)

@Injected
data class Nested(private val dependency: IDependency): INested