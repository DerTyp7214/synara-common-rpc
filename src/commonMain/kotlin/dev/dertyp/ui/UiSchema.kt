package dev.dertyp.ui

import kotlin.reflect.KClass

object UiSchema {
    val introducedIn: Map<KClass<out UiComponent>, Int> = mapOf(
        UiComponent.Column::class to 1,
        UiComponent.Row::class to 1,
        UiComponent.Grid::class to 1,
        UiComponent.Card::class to 1,
        UiComponent.Section::class to 1,
        UiComponent.Form::class to 1,
        UiComponent.Text::class to 1,
        UiComponent.Icon::class to 1,
        UiComponent.Image::class to 1,
        UiComponent.Badge::class to 1,
        UiComponent.Stat::class to 1,
        UiComponent.Progress::class to 1,
        UiComponent.Tile::class to 1,
        UiComponent.Button::class to 1,
        UiComponent.ListItem::class to 1,
        UiComponent.Table::class to 1,
        UiComponent.Spacer::class to 1,
        UiComponent.Divider::class to 1,
        UiComponent.Fallback::class to 1,
        UiComponent.Native::class to 1,
        UiComponent.Log::class to 1,
        UiComponent.Live::class to 1,
        UiComponent.TextField::class to 1,
        UiComponent.NumberField::class to 1,
        UiComponent.Switch::class to 1,
        UiComponent.Select::class to 1,
    )

    fun versionOf(component: UiComponent): Int = introducedIn[component::class] ?: UiSchemaVersion.CURRENT
}
