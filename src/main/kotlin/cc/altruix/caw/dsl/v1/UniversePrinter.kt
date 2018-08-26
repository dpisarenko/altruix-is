package cc.altruix.caw.dsl.v1

import cc.altruix.utils.nl

/**
 * Created by pisarenko on 24.05.2017.
 */
class UniversePrinter {
    fun toString(u:Universes):String {
        val sb = StringBuilder()
        sb.append("import cc.altruix.caw.dsl.v1.Scene")
        sb.nl()
        sb.append("import cc.altruix.caw.dsl.v1.Character")
        sb.nl()
        sb.append("import cc.altruix.caw.dsl.v1.Place")
        sb.nl()
        sb.append("import cc.altruix.caw.dsl.v1.GroupOfCharacters")
        sb.nl()

        sb.nl()

        sb.append("fun Universe() {")
        sb.nl()
        sb.nl()

        printCharacters(u.characters, sb)
        printGroupsOfCharacters(u.groupsOfCharacters, sb)
        printPlaces(u.places, sb)
        printScenes(u.scenes, sb)

        sb.nl()
        sb.nl()
        sb.append("}")
        return sb.toString()
    }

    private fun printScenes(scenes: List<Scene>, sb: StringBuilder) {
        scenes.sortedBy {
            it.id.removePrefix("sc")
                    .replace("f", ".5")
                    .replace("a", ".1")
                    .replace("b", ".2")
                    .replace("c", ".3")
                    .toDouble()
                    .times(100)
                    .toInt()
        }.forEach { scene ->
            val id = scene.id
            val name = scene.name
            sb.append("val $id = Scene(\"$id\", \"$name\")")
            sb.nl()

            scene.notes.forEach { note ->
                sb.append("$id.addNote(\"\"\"")
                sb.nl()
                sb.append(note)
                sb.nl()
                sb.append("\"\"\")")
                sb.nl()
            }

            sb.nl()
            sb.nl()
        }
    }

    private fun printPlaces(places: List<Place>, sb: StringBuilder) {
        places.sortedBy { it.id }
                .forEach { place ->
                    val id = place.id
                    val name = place.name
                    sb.append("val $id = Place(\"$id\", \"$name\")")
                    sb.nl()
                }
    }

    private fun printGroupsOfCharacters(groupsOfCharacters: List<GroupOfCharacters>, sb: StringBuilder) {
        groupsOfCharacters.sortedBy { it.id }
                .forEach { groupOfCharacters ->
                    val id = groupOfCharacters.id
                    val name = groupOfCharacters.name
                    sb.append("val $id = GroupOfCharacters(\"$id\", \"$name\")")
                    sb.nl()
                }
    }
    private fun printCharacters(characters: List<Character>, sb: StringBuilder) {
        characters.sortedBy { it.id }
                .forEach { character ->
                    val id = character.id
                    val name = character.name
                    sb.append("val $id = Character(\"$id\", \"$name\")")
                    sb.nl()
                }
    }
}