package cc.altruix.is1.capsulecrm

/**
 * Created by pisarenko on 31.01.2017.
 */
open class PartiesSearchResult(
        success:Boolean,
        errorMsg:String,
        val parties:List<Party>
) : CapsuleCrmInteractionResult(success, errorMsg) {
}