package com.lostagain.gdxscoretester.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.lostagain.gdxscoretester.GdxScoreTester;

public class HtmlLauncher extends GwtApplication {



	@Override
	public void onModuleLoad () {
		super.onModuleLoad();
		com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent ev) {

				//resize canvas to html page size
				Gdx.graphics.setWindowedMode(ev.getWidth(),ev.getHeight());

				//tell game to update if needed
				//...
			}
		});
	}




	@Override
	public GwtApplicationConfiguration getConfig () {

		GwtApplicationConfiguration config = new GwtApplicationConfiguration(Window.getClientWidth(), Window.getClientHeight());

		//
		//instead of default fixed size, the following code will put it in a 100%/100% sized panel
		//

		com.google.gwt.dom.client.Element element = Document.get().getElementById("embed-html"); //gets the div where it normally goes

		//create replacement panel;
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		panel.setHeight("100%");
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		//
		element.appendChild(panel.getElement()); //add new panel where other one was

		config.rootPanel = panel; //set the condig to use it

		return config; //480,320 is default
	}



	@Override
	public ApplicationListener createApplicationListener () {
		return new GdxScoreTester();
	}
}