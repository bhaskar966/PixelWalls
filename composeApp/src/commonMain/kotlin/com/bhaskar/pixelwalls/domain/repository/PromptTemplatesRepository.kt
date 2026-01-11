package com.bhaskar.pixelwalls.domain.repository

import com.bhaskar.pixelwalls.domain.model.PromptCategory
import com.bhaskar.pixelwalls.domain.model.PromptOption
import com.bhaskar.pixelwalls.domain.model.PromptTemplate
import com.bhaskar.pixelwalls.domain.model.PromptVariable

object PromptTemplatesRepository {

    private val subjects = PromptVariable(
        key = "subject",
        displayName = "Subject",
        options = listOf(
            PromptOption("castle", "🏰"),
            PromptOption("bicycle", "🚲"),
            PromptOption("building", "🏢"),
            PromptOption("boat", "⛵"),
            PromptOption("lamp", "💡"),
            PromptOption("table", "🪑"),
            PromptOption("bridge", "🌉"),
            PromptOption("lighthouse", "🗼"),
            PromptOption("tree", "🌳"),
            PromptOption("flower", "🌸"),
            PromptOption("mountain", "⛰️"),
            PromptOption("car", "🚗")
        )
    )

    private val materials = PromptVariable(
        key = "material",
        displayName = "Material",
        options = listOf(
            PromptOption("opal", "💎"),
            PromptOption("flowers", "🌺"),
            PromptOption("chenille", "🧶"),
            PromptOption("clouds", "☁️"),
            PromptOption("water", "💧"),
            PromptOption("fire", "🔥"),
            PromptOption("glass", "🪟"),
            PromptOption("crystal", "💎"),
            PromptOption("marble", "🪨"),
            PromptOption("metal", "⚙️"),
            PromptOption("wood", "🪵"),
            PromptOption("ice", "🧊"),
            PromptOption("sand", "🏖️"),
            PromptOption("moss", "🌿")
        )
    )

    private val colors1 = PromptVariable(
        key = "color1",
        displayName = "Color 1",
        options = listOf(
            PromptOption("coral", "🪸"),
            PromptOption("pink", "🌸"),
            PromptOption("green", "🍏"),
            PromptOption("blue", "💙"),
            PromptOption("purple", "💜"),
            PromptOption("red", "❤️"),
            PromptOption("orange", "🧡"),
            PromptOption("yellow", "💛"),
            PromptOption("teal", "🩵"),
            PromptOption("gold", "✨"),
            PromptOption("silver", "⚪"),
            PromptOption("emerald", "💚")
        )
    )

    private val colors2 = PromptVariable(
        key = "color2",
        displayName = "Color 2",
        options = listOf(
            PromptOption("tan", "🟤"),
            PromptOption("purple", "💜"),
            PromptOption("teal", "🩵"),
            PromptOption("blue", "💙"),
            PromptOption("pink", "💗"),
            PromptOption("green", "💚"),
            PromptOption("orange", "🧡"),
            PromptOption("white", "🤍"),
            PromptOption("black", "🖤"),
            PromptOption("crimson", "❤️"),
            PromptOption("violet", "💜"),
            PromptOption("amber", "🟡")
        )
    )

    private val styles = PromptVariable(
        key = "style",
        displayName = "Style",
        options = listOf(
            PromptOption("surreal"),
            PromptOption("dreamlike"),
            PromptOption("ethereal"),
            PromptOption("mystical"),
            PromptOption("enchanted"),
            PromptOption("magical"),
            PromptOption("fantastical"),
            PromptOption("whimsical")
        )
    )

    val templates = listOf(
        PromptTemplate(
            id = "template_1",
            category = PromptCategory.IMAGINARY,
            template = "A {style} {subject} made of {material} in shades of {color1} and {color2}",
            fixedWords = listOf("A", "made of", "in shades of", "and"),
            variables = mapOf(
                "style" to styles,
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_2",
            category = PromptCategory.NATURE,
            template = "An enchanted {subject} glowing with {material} under {color1} and {color2} lights",
            fixedWords = listOf("An", "enchanted", "glowing with", "under", "and", "lights"),
            variables = mapOf(
                "subject" to subjects.copy(
                    options = listOf(
                        PromptOption("forest", "🌲"),
                        PromptOption("garden", "🌷"),
                        PromptOption("waterfall", "💦"),
                        PromptOption("meadow", "🌾"),
                        PromptOption("lake", "🏞️"),
                        PromptOption("canyon", "🏜️")
                    )
                ),
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_3",
            category = PromptCategory.ABSTRACT,
            template = "Flowing {material} forming abstract {subject} patterns in {color1} and {color2}",
            fixedWords = listOf("Flowing", "forming abstract", "patterns in", "and"),
            variables = mapOf(
                "subject" to subjects.copy(
                    options = listOf(
                        PromptOption("geometric"),
                        PromptOption("spiral"),
                        PromptOption("wave"),
                        PromptOption("circular"),
                        PromptOption("fractal"),
                        PromptOption("crystalline")
                    )
                ),
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_4",
            category = PromptCategory.ARCHITECTURAL,
            template = "A majestic {subject} crafted from {material} with {color1} and {color2} accents",
            fixedWords = listOf("A", "majestic", "crafted from", "with", "and", "accents"),
            variables = mapOf(
                "subject" to subjects.copy(
                    options = listOf(
                        PromptOption("palace", "🏰"),
                        PromptOption("temple", "🛕"),
                        PromptOption("tower", "🗼"),
                        PromptOption("cathedral", "⛪"),
                        PromptOption("monument", "🗿"),
                        PromptOption("pavilion", "🏛️")
                    )
                ),
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_5",
            category = PromptCategory.FANTASY,
            template = "A mystical {subject} floating among {material} clouds in {color1} and {color2} hues",
            fixedWords = listOf("A", "mystical", "floating among", "clouds in", "and", "hues"),
            variables = mapOf(
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_6",
            category = PromptCategory.IMAGINARY,
            template = "A {style} {subject} surrounded by {material} in vibrant {color1} and {color2}",
            fixedWords = listOf("A", "surrounded by", "in vibrant", "and"),
            variables = mapOf(
                "style" to styles,
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_7",
            category = PromptCategory.NATURE,
            template = "A radiant {subject} blooming with {material} textures in {color1} and {color2} tones",
            fixedWords = listOf("A", "radiant", "blooming with", "textures in", "and", "tones"),
            variables = mapOf(
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_8",
            category = PromptCategory.ABSTRACT,
            template = "Luminous {material} swirling into {subject} shapes with {color1} and {color2} gradients",
            fixedWords = listOf("Luminous", "swirling into", "shapes with", "and", "gradients"),
            variables = mapOf(
                "subject" to subjects.copy(
                    options = listOf(
                        PromptOption("spiral"),
                        PromptOption("vortex"),
                        PromptOption("cosmic"),
                        PromptOption("organic"),
                        PromptOption("flowing")
                    )
                ),
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_9",
            category = PromptCategory.FANTASY,
            template = "A celestial {subject} woven from {material} glowing in {color1} and {color2}",
            fixedWords = listOf("A", "celestial", "woven from", "glowing in", "and"),
            variables = mapOf(
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        ),

        PromptTemplate(
            id = "template_10",
            category = PromptCategory.IMAGINARY,
            template = "An otherworldly {subject} composed of {material} bathed in {color1} and {color2} light",
            fixedWords = listOf("An", "otherworldly", "composed of", "bathed in", "and", "light"),
            variables = mapOf(
                "subject" to subjects,
                "material" to materials,
                "color1" to colors1,
                "color2" to colors2
            )
        )
    )

    fun getRandomSelection(templateId: String): Map<String, Int> {
        val template = templates.find { it.id == templateId } ?: return emptyMap()
        return template.variables.mapValues { (_, variable) ->
            variable.options.indices.random()
        }
    }

    fun buildPrompt(template: PromptTemplate, selection: Map<String, Int>): String {
        var prompt = template.template
        selection.forEach { (key, index) ->
            val variable = template.variables[key]
            val value = variable?.options?.getOrNull(index)?.value ?: ""
            prompt = prompt.replace("{$key}", value)
        }
        return prompt
    }
}
