package com.siberika.idea.pascal.jps.builder;

import com.siberika.idea.pascal.jps.compiler.CompilerMessager;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static com.siberika.idea.pascal.jps.compiler.DelphiBackendCompiler.DELPHI_STARTER_RESPONSE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Author: George Bakhtadze
 * Date: 12/04/2017
 */
public class DelphiCompilerProcessAdapterTest {

    private DelphiCompilerProcessAdapter adapter;
    private CompileContext context;
    private CompilerMessager messager;

    @Before
    public void setUp() throws Exception {
        context = mock(CompileContext.class);
        messager = new PascalCompilerMessager("test", context);
        adapter = new DelphiCompilerProcessAdapter(messager);
    }

    @Test
    public void processLine() throws Exception {
        adapter.processLine(messager, "c:\\srv\\test.pas(15) Error: E2003 Undeclared identifier");
        ArgumentCaptor<CompilerMessage> argument = ArgumentCaptor.forClass(CompilerMessage.class);
        verify(context).processMessage(argument.capture());
        assertEquals(BuildMessage.Kind.ERROR, argument.getValue().getKind());
        assertEquals("E2003 Undeclared identifier", argument.getValue().getMessageText());
        assertEquals(15, argument.getValue().getLine());
        assertEquals(-1, argument.getValue().getColumn());
        assertEquals("c:\\srv\\test.pas", argument.getValue().getSourcePath());
    }

    @Test
    public void starter() throws Exception {
        adapter.processLine(messager, DELPHI_STARTER_RESPONSE + ".");
        ArgumentCaptor<CompilerMessage> argument = ArgumentCaptor.forClass(CompilerMessage.class);
        verify(context).processMessage(argument.capture());
        assertEquals(BuildMessage.Kind.ERROR, argument.getValue().getKind());
    }

}