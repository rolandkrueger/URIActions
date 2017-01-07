package org.vaadin.uriactions;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewDisplay;
import org.junit.Before;
import org.junit.Test;
import org.roklib.urifragmentrouting.UriActionCommand;
import org.roklib.urifragmentrouting.UriActionMapperTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.uriactions.testhelpers.TestNavigationStateHandler;
import org.vaadin.uriactions.testhelpers.TestUI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UriFragmentActionNavigatorTest {
    private final static Logger LOG = LoggerFactory.getLogger(UriFragmentActionNavigatorTest.class);

    private UriFragmentActionNavigator uriFragmentActionNavigator;
    private TestNavigationStateHandler navigationStateHandler;
    private UriActionMapperTree uriActionMapperTree;

    @Before
    public void setUp() {
        navigationStateHandler = new TestNavigationStateHandler();
        uriFragmentActionNavigator = new UriFragmentActionNavigator(new TestUI(), navigationStateHandler);
        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree().build();
    }

    @Test
    public void testAddHandler() {
        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onAction(TestActionCommand.class)
                .finishMapper().build();

        uriFragmentActionNavigator.setUriActionMapperTree(uriActionMapperTree);
        uriFragmentActionNavigator.getNavigator().addViewChangeListener(new ViewChangeListener() {
            @Override
            public boolean beforeViewChange(final ViewChangeEvent event) {
                LOG.info("Before view change. New view: {}", event.getNewView());
                return true;
            }

            @Override
            public void afterViewChange(final ViewChangeEvent event) {
                LOG.info("After view change. New view: {}", event.getNewView());
                final UriFragmentActionNavigator.ActionExecutionView view = (UriFragmentActionNavigator.ActionExecutionView) event.getNewView();
                final TestActionCommand uriActionCommand = (TestActionCommand) view.getUriActionCommand();
                assertTrue("Action command was not executed.", uriActionCommand.isExecuted());
            }
        });

        navigationStateHandler.setState("/test");
        uriFragmentActionNavigator.getNavigator().navigateTo("/test");
    }

    @Test
    public void testProvideOwnViewDisplay() {
        final TestViewDisplay viewDisplay = new TestViewDisplay();
        uriFragmentActionNavigator = new UriFragmentActionNavigator(new TestUI(), navigationStateHandler, viewDisplay);
        final TestActionCommand cmd = new TestActionCommand();

        uriActionMapperTree = UriActionMapperTree.create().buildMapperTree()
                .map("test")
                .onAction(TestActionCommand.class)
                .finishMapper().build();

        uriFragmentActionNavigator.setUriActionMapperTree(uriActionMapperTree);
        uriFragmentActionNavigator.getNavigator().addView("separate_view", (View) event -> {
        });

        uriFragmentActionNavigator.getNavigator().navigateTo("separate_view");
        assertTrue("Separately provided view display was not activated.", viewDisplay.viewShown);
        assertFalse("Action command was unexpectedly executed.", cmd.isExecuted());
    }

    private static class TestViewDisplay implements ViewDisplay {
        public boolean viewShown = false;

        @Override
        public void showView(final View view) {
            viewShown = true;
        }
    }

    public static class TestActionCommand implements UriActionCommand {
        private boolean executed = false;

        @Override
        public void run() {
            executed = true;
        }

        public boolean isExecuted() {
            return executed;
        }
    }
}
