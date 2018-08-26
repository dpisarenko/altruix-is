package cc.altruix.caw.dsl.v1

/**
 * Created by pisarenko on 11.05.2017.
 */
class Scene(val id:String, val name:String) {
    val notes = ArrayList<String>(1)
    fun addNote(note: String) {
        notes.add(note)
    }
}