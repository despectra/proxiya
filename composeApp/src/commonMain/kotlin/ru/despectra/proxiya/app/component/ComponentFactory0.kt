package ru.despectra.proxiya.app.component

import com.arkivanov.decompose.ComponentContext

/**
 * @param C тип создаваемого компонента
 */
interface ComponentFactory0<C : Any> {
    fun create(componentContext: ComponentContext) : C
}