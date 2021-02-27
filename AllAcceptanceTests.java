package com.vsn;


import com.vsn.acceptanceTests.AccountLifeCycleTest;
import com.vsn.acceptanceTests.AddStickyNoteToBoardTest;
import com.vsn.acceptanceTests.AddTextToStickyNoteTest;
import com.vsn.acceptanceTests.ChangePasswordEmailTest;
import com.vsn.acceptanceTests.ClearBoardTest;
import com.vsn.acceptanceTests.CloseAppSignInTest;
import com.vsn.acceptanceTests.CreateAccountLogInTest;
import com.vsn.acceptanceTests.DeleteAccountTest;
import com.vsn.acceptanceTests.EditStickyNoteTest;
import com.vsn.acceptanceTests.ListOfBoardsTest;
import com.vsn.acceptanceTests.LogOutSwitchAccountTest;
import com.vsn.acceptanceTests.MakeBoardTest;
import com.vsn.acceptanceTests.MoveStickyNotesTest;
import com.vsn.acceptanceTests.OpenBoardsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
@RunWith(Suite.class)
//The following enables this file to run all tests when right-clicked ->
//run "AllAcceptanceTests", update the arguments as new test classes are created.
//May take several minutes to run
@Suite.SuiteClasses({
        AccountLifeCycleTest.class,
        AddStickyNoteToBoardTest.class,
        AddTextToStickyNoteTest.class,
        ChangePasswordEmailTest.class,
        ClearBoardTest.class,
        CloseAppSignInTest.class,
        CreateAccountLogInTest.class,
        DeleteAccountTest.class,
        EditStickyNoteTest.class,
        ListOfBoardsTest.class,
        LogOutSwitchAccountTest.class,
        MakeBoardTest.class,
        MoveStickyNotesTest.class,
        OpenBoardsTest.class
})

public class AllAcceptanceTests {

}