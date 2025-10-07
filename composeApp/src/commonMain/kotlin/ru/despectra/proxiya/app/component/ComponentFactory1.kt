package ru.despectra.proxiya.app.component

import com.arkivanov.decompose.ComponentContext

/**
 * @param C тип создаваемого компонента
 * @param P тип аргумента фабрики
 */
interface ComponentFactory1<C : Any, P : Any> {
    fun create(param: P, componentContext: ComponentContext) : C
}