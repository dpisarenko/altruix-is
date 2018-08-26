package cc.altruix.caw.dsl

import cc.altruix.caw.dsl.v1.*
import cc.altruix.utils.isNumeric
import org.apache.commons.io.IOUtils
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant

/**
 * Created by pisarenko on 10.05.2017.
 */
class PlotFileParser {
    companion object {
        val DateFormat = SimpleDateFormat("dd.MM.yyyy")
        val DateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

        val DatePartRegex = "\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d"
        val TimeRegex = "\\d\\d\\:\\d\\d"
        val DateTimeRegex = Regex("$DatePartRegex $TimeRegex")

    }
    fun parse(stream:InputStream): Universes {
        val txt = IOUtils.toString(stream, "UTF-8")
        val characters = extractCharacters(txt)
        val groupsOfCharacters = extractGroupsOfCharacters(txt)
        val places = extractPlaces(txt)
        val scenes = extractScenes(txt)
        val timeInstants = extractTimeInstants(txt)
        val timeInstantNotes = extractTimeInstantNotes(txt)
        val timeInstantsById = HashMap<String,TimeInstant>()
        timeInstants.forEach {
            timeInstantsById[it.id] = it
        }
        setTimeInstantsDateTimes(timeInstantNotes, timeInstantsById)
        addSceneNotes(scenes, txt)
        return Universes(
                characters,
                groupsOfCharacters,
                places,
                scenes,
                timeInstants
        )
    }

    private fun addSceneNotes(scenes: List<Scene>, txt: String) =
            scenes.forEach { addNotes(it, txt) }

    private fun addNotes(scene: Scene, txt: String) {
        val id = scene.id
        val ot = "<${id}_desc>"
        val ct = "</${id}_desc>"

        val otStart = txt.indexOf(ot)
        if (otStart < 0) {
            return
        }

        val ctStart = txt.indexOf(ct)
        if (ctStart < 0) {
            return
        }

        val rawNote = txt.substring(otStart + ot.length, ctStart)

        val note = rawNote.replace("\n# ", "\n").replace("\n#", "\n")
        scene.addNote(note)
    }

    private fun setTimeInstantsDateTimes(timeInstantNotes: List<TimeInstantNote>, timeInstantsById: HashMap<String, TimeInstant>) {
        timeInstantNotes.forEach { note ->
            val dateFormat = dateFormat(note.label)
            val timeFormat = timeFormat(note.label)

            if (!dateFormat && !timeFormat) {
                System.out.println("Error 1 ('${note.instantId}', '${note.label}')")
                return@forEach
            }
            val instantId = note.instantId.removeSuffix("_note")
            val instant = timeInstantsById[instantId]
            if (instant == null) {
                System.out.println("Error 2")
                return@forEach
            }
            val instantDateTime: Instant =
                    extractInstantDateTime(
                            note.label,
                            dateFormat,
                            timeFormat
                    )
            instant.setInstant(instantDateTime)
        }
    }

    open fun extractInstantDateTime(
            txt: String,
            dateFormat: Boolean,
            timeFormat: Boolean
    ): Instant {
        if (dateFormat) {
            return DateFormat.parse(txt).toInstant()
        } else if (timeFormat) {
            return DateTimeFormat.parse(txt).toInstant()
        } else {
            throw RuntimeException("Unexpected date/time format")
        }
    }

    open fun dateFormat(label:String): Boolean =
            (label != null) &&
                    (label.length == 10) &&
                    (label[2] == '.') &&
                    (label[5] == '.')

    open fun timeFormat(label:String): Boolean =
            DateTimeRegex.matches(label)

    private fun extractTimeInstantNotes(txt: String): List<TimeInstantNote> {
        val timeInstantNotes = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("t")
                        && it.contains("[label=\"")
                        && (it.length > 2)
                        && !it.contains("->")
                        && it.contains("_note")
                        && !it.contains("_dt")
                }
                .map { parseTimeInstantNoteDeclaration(it) }
                .filter { it != null }
                .toList() as List<TimeInstantNote>
        return timeInstantNotes
    }

    protected fun extractTimeInstants(txt: String): List<TimeInstant> {
        val instants = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("t")
                        && it.contains("[label=\"")
                        && (it.length > 2)
                        && !it.contains("->")
                        && !it.contains("_note")
                        && !it.contains("_dt")
                }
                .map { parseTimeInstantDeclaration(it) }
                .filter { it != null }
                .toList() as List<TimeInstant>
        return instants
    }


    protected fun extractScenes(txt: String): List<Scene> {
        val scenes = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("sc") && it.contains("[label=\"") && (it.length > 2) && !it.contains("->") && !it.contains("_note") }
                .filter {
                    (it.substring("sc".length, "sc".length + 2).isNumeric() || it.substring("sc".length, "sc".length + 1).isNumeric())
                }
                .map { parseSceneDeclaration(it) }
                .filter { it != null }
                .toList() as List<Scene>
        return scenes
    }

    protected fun extractPlaces(txt: String): List<Place> {
        val places = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("p_") && !it.contains("->") && !it.contains("_note") }
                .map { parsePlaceDeclaration(it) }
                .filter { it != null }
                .toList() as List<Place>
        return places
    }

    private fun extractGroupsOfCharacters(txt: String): List<GroupOfCharacters> {
        val groupsOfCharacters = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("cg_") && !it.contains("->") && !it.contains("_note") }
                .map { parseCharacterGroupDeclaration(it) }
                .filter { it != null }
                .toList() as List<GroupOfCharacters>
        return groupsOfCharacters
    }

    protected fun extractCharacters(txt: String): List<Character> {
        val characters = txt
                .lines()
                .map { it.trim() }
                .filter { it.startsWith("c_") && !it.contains("->") && !it.contains("_note") }
                .map { parseCharacterDeclaration(it) }
                .filter { it != null }
                .toList() as List<Character>
        return characters
    }
    fun parseTimeInstantNoteDeclaration(txt:String): TimeInstantNote? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return TimeInstantNote(id, name)
    }

    fun parseTimeInstantDeclaration(txt:String): TimeInstant? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return TimeInstant(id, name)
    }

    fun parseSceneDeclaration(txt:String): Scene? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return Scene(id, name)
    }

    fun parsePlaceDeclaration(txt:String): Place? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return Place(id, name)
    }

    fun parseCharacterGroupDeclaration(txt:String): GroupOfCharacters? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return GroupOfCharacters(id, name)
    }
    fun parseCharacterDeclaration(txt:String): Character? {
        val idAndName = extractIdAndName(txt)
        if (idAndName == null) {
            return null
        }
        val (id, name) = idAndName
        return Character(id, name)
    }

    fun extractIdAndName(txt:String):Pair<String,String>? {
        val descIdx = txt.indexOf('[')
        if (descIdx < 0) {
            return null
        }
        val id = txt.substring(0, descIdx)
        var nameStart = txt.indexOf("label=\"")
        if (nameStart < 0) {
            return null
        }
        nameStart += "label=\"".length
        val nameEnd = txt.indexOf("\"", nameStart)
        if (nameEnd < 0) {
            return null
        }
        val name = txt.substring(nameStart, nameEnd)
        return Pair(id.trim(), name.trim())
    }
}