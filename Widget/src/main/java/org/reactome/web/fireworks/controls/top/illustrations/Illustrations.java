package org.reactome.web.fireworks.controls.top.illustrations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import org.reactome.web.fireworks.client.FireworksFactory;
import org.reactome.web.fireworks.controls.top.common.AbstractMenuDialog;
import org.reactome.web.fireworks.events.IllustrationSelectedEvent;
import org.reactome.web.fireworks.events.NodeOpenedEvent;
import org.reactome.web.fireworks.events.NodeSelectedEvent;
import org.reactome.web.fireworks.events.NodeSelectedResetEvent;
import org.reactome.web.fireworks.handlers.NodeOpenedHandler;
import org.reactome.web.fireworks.handlers.NodeSelectedHandler;
import org.reactome.web.fireworks.handlers.NodeSelectedResetHandler;
import org.reactome.web.fireworks.model.Node;
import org.reactome.web.fireworks.util.Console;
import org.reactome.web.pwp.model.classes.DatabaseObject;
import org.reactome.web.pwp.model.classes.Figure;
import org.reactome.web.pwp.model.classes.Pathway;
import org.reactome.web.pwp.model.factory.DatabaseObjectFactory;
import org.reactome.web.pwp.model.handlers.DatabaseObjectCreatedHandler;
import org.reactome.web.pwp.model.handlers.DatabaseObjectLoadedHandler;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class Illustrations extends AbstractMenuDialog implements NodeOpenedHandler,
        NodeSelectedHandler, NodeSelectedResetHandler {

    private EventBus eventBus;

    public Illustrations(EventBus eventBus) {
        super("Illustrations");
        addStyleName(RESOURCES.getCSS().illustrations());

        this.eventBus = eventBus;
        this.initHandlers();

        initialise();
        setInitialMessage();
    }

    @Override
    public void onNodeOpened(NodeOpenedEvent event) {
        hide();
    }

    @Override
    public void onNodeSelected(NodeSelectedEvent event) {
        Node node = event.getNode();
        setIllustrations(node.getDbId());
    }

    @Override
    public void onNodeSelectionReset() {
        setInitialMessage();
    }

    private void setInitialMessage(){
        clear();
        Label warning = new Label("No pathway selected");
        warning.setStyleName(RESOURCES.getCSS().warning());
        add(warning);
    }

    private void setIllustrations(Long dbId){
        clear();
        if(dbId==null) return;
        Label loadingLbl = new Label("Loading...");
        loadingLbl.setStyleName(RESOURCES.getCSS().loading());
        add(loadingLbl);
        DatabaseObjectFactory.get(dbId, new DatabaseObjectCreatedHandler() {
            @Override
            public void onDatabaseObjectLoaded(DatabaseObject databaseObject) {
                if (databaseObject instanceof Pathway) {
                    clear();
                    final Pathway pathway = (Pathway) databaseObject;
                    if (pathway.getFigure().isEmpty()) {
                        add(getIllustration(pathway, null));
                    } else {
                        for (Figure figure : pathway.getFigure()) {
                            figure.load(new DatabaseObjectLoadedHandler() {
                                @Override
                                public void onDatabaseObjectLoaded(DatabaseObject databaseObject) {
                                    Figure figure = (Figure) databaseObject;
                                    add(getIllustration(pathway, FireworksFactory.ILLUSTRATION_SERVER + figure.getUrl()));
                                }

                                @Override
                                public void onDatabaseObjectError(Throwable throwable) {
                                    Console.error(throwable.getMessage());
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onDatabaseObjectError(Throwable exception) {
                clear();
                add(getErrorMsg("There was an error retrieving data for the loaded diagram..."));
            }
        });
    }

    private Widget getIllustration(Pathway pathway, final String url) {
        if (url != null && !url.isEmpty()) {
            FlowPanel fp = new FlowPanel();
            fp.setStyleName(RESOURCES.getCSS().illustration());
            Image image = new Image(RESOURCES.illustration());
            fp.add(image);
            Label label = new Label(pathway.getDisplayName());
            label.setText(pathway.getDisplayName());
            fp.add(label);
            Anchor anchor = new Anchor(SafeHtmlUtils.fromTrustedString(fp.toString()), url);
            anchor.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!event.isMetaKeyDown() && !event.isControlKeyDown()) event.preventDefault();
                    event.stopPropagation();
                    hide();
                    eventBus.fireEventFromSource(new IllustrationSelectedEvent(url), Illustrations.this);
                }
            });
            return anchor;
        } else {
            return getErrorMsg("No illustrations for " + pathway.getDisplayName());
        }
    }

    private Widget getErrorMsg(String msg){
        Label label = new Label(msg);
        label.setTitle(msg);
        label.setStyleName(RESOURCES.getCSS().error());
        return label;
    }

    private void initialise(){
        clear();
    }

    private void initHandlers() {
        this.eventBus.addHandler(NodeOpenedEvent.TYPE, this);
        this.eventBus.addHandler(NodeSelectedEvent.TYPE, this);
        this.eventBus.addHandler(NodeSelectedResetEvent.TYPE, this);
    }


    public static Resources RESOURCES;
    static {
        RESOURCES = GWT.create(Resources.class);
        RESOURCES.getCSS().ensureInjected();
    }

    public interface Resources extends ClientBundle {
        @Source(ResourceCSS.CSS)
        ResourceCSS getCSS();

        @Source("images/illustration.png")
        ImageResource illustration();
    }

    @CssResource.ImportedWithPrefix("fireworks-Illustrations")
    public interface ResourceCSS extends CssResource {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String CSS = "org/reactome/web/fireworks/controls/top/illustrations/DiagramIllustrations.css";

        String illustrations();

        String illustration();

        String error();

        String warning();

        String loading();
    }
}
