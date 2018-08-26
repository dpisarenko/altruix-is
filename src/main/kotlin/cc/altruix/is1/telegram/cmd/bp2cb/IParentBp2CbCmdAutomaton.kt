package cc.altruix.is1.telegram.cmd.bp2cb

import cc.altruix.is1.telegram.IParentAutomaton
import java.io.File

/**
 * Created by 1 on 25.02.2017.
 */
interface IParentBp2CbCmdAutomaton : IParentAutomaton<Bp2CbCmdState> {
    fun readFileContents(fileId:String): File?
    fun persona():String
}