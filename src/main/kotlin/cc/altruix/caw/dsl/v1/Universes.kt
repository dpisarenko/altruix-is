package cc.altruix.caw.dsl.v1

/**
 * Created by pisarenko on 10.05.2017.
 */
class Universes(
        val characters: List<Character>,
        val groupsOfCharacters: List<GroupOfCharacters>,
        val places: List<Place>,
        val scenes: List<Scene>, val timeInstants: List<TimeInstant>
) {
}