package cc.altruix.is1.telegram.bots.herrKarl

import org.junit.Test
import cc.altruix.mock
import org.fest.assertions.Assertions.assertThat
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.telegram.telegrambots.api.objects.Message
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.api.objects.User

/**
 * Created by 1 on 20.02.2017.
 */
class WolfTests {
    @Test
    fun rightUser() {
        rightUserTestLogic(240210357, true)
        rightUserTestLogic(240210356, false)
        rightUserTestLogic(240210358, false)
    }

    private fun rightUserTestLogic(userId: Int, expRes: Boolean) {
        // Prepare
        val sut = Wolf()
        val update = mock<Update>()
        val message = mock<Message>()
        val user = mock<User>()
        `when`(user.id).thenReturn(userId)
        `when`(message.from).thenReturn(user)
        `when`(update.message).thenReturn(message)

        // Run method under test
        val actRes = sut.rightUser(update)

        // Verify
        assertThat(actRes).isEqualTo(expRes)
    }
}