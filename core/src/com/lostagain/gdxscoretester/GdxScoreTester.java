package com.lostagain.gdxscoretester;

import java.util.logging.Logger;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.google.common.base.Strings;
import com.lostagain.gdxscoretester.shadertests.GwtishWidgetShaderAttribute;
import com.lostagain.gdxscoretester.shadertests.MyShaderProvider;
import com.lostagain.nl.GWTish.Button;
import com.lostagain.nl.GWTish.ClickHandler;
import com.lostagain.nl.GWTish.Label;
import com.lostagain.nl.GWTish.Style.Unit;
import com.lostagain.nl.GWTish.Management.GWTishModelManagement;

public class GdxScoreTester extends ApplicationAdapter {

	final static String logstag = "GdxScoreTester";
	public static Logger Log = Logger.getLogger(logstag); //not we are using this rather then gdxs to allow level control per tag


	PerspectiveCamera cam;


	//for shader testing only';
	public static MyShaderProvider myshaderprovider = new MyShaderProvider();
	//
	public CameraInputController camController;



	@Override
	public void create () {

		Log.info("setup started");


		float w = Gdx.graphics.getWidth();		
		float h = Gdx.graphics.getHeight();

		cam = new PerspectiveCamera(67,w,h);

		cam.position.set(0f, 0f, 225f); //overhead
		cam.lookAt(0,0,0);
		cam.near = 0.5f;
		cam.far = 500f;
		cam.update();

		Log.info("camera setup ");
		
		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		Log.info("camera controller setup ");
		
		//GWTish has its own model manager that must be setup
		//This is required for distance field fonts to render correctly.
		GWTishModelManagement.setup(); 

		Log.info("GWTishModelManagement setup  ");
		
		//addTestModels();
		//textTests();		

		scoreTests();


		Log.info("setup ended");

	}

	/**
	 * sets up a few different types of score counters, as well as buttons to test them
	 */
	private void scoreTests() {

		//
		//create different types of score labels
		//Note: No optimisation has been done. The String manipulation based ones could be done better ways.
		//
		
		//Fixed 5 second basic lerp. 
		final GenericScoreDisplayer fivesecondLerp = new GenericScoreDisplayer(){
			@Override
			public void updateDisplayedScoreVar(float secs) {
				
				float deltams = secs *1000;
				currentTime=currentTime+deltams; //cur time
				double alpha = currentTime/5000.0; //0.0=start 1.0 = done (alpha in this context just means representing how far we are between two states, its got nothing to do with opacity)


				CurrentlyDisplayedScore = initialScore+((CurrentScore-initialScore)*alpha); //temp, basic lerp
			}

		};
		
		
		//Default
		//(time taken is based on digits to change, upto 7 seconds)
		final GenericScoreDisplayer darksStandard = new GenericScoreDisplayer();
		
		//
		//gumboots / KlaxonCow suggestion
		//new_display_score = current_score - floor((current_score - old_display_score) * k0);
		//
		final GenericScoreDisplayer constantDigits = new GenericScoreDisplayer(){
			@Override
			public void updateDisplayedScoreVar(float secs) {
				//we replace the formular used to update
				double sub = Math.floor((CurrentScore - CurrentlyDisplayedScore) * 0.9);
				Log.info("sub:"+sub);
				if (direction==scoreChangeDirection.countDown && sub<0){
					sub=1; //min speeed, should never go negative
					//gets stuck at -9 without this
				}
				CurrentlyDisplayedScore = CurrentScore - sub;
				
			}

		};

		//
		//randomise the digits till correct
		//(does not reset/remove digits nicely)
		final GenericScoreDisplayer randomDigits = new GenericScoreDisplayer(){
			@Override
			public void updateDisplayedScoreVar(float secs) {

				//current as string
				String curAsString =""+CurrentlyDisplayedScore;
				//get tqarget length
				String targetASstring = ""+CurrentScore;
				int len = targetASstring.length();

				String newScoreVis = "";
				//loop for each digit
				for (int i = 0; i < len; i++) {
					String newchar = "";
					if (i>(curAsString.length()-1)){
						newchar="0";
					} else {
						newchar=curAsString.substring(i, i+1);
					}

					String tar = targetASstring.substring(i, i+1);
					//if correct and randomredo=false then keep
					if (newchar.equals(tar) ){
						newScoreVis=newScoreVis+tar;
					//	Log.info("never get here");
					}else {
						//else give random num
						int ran = (int) (Math.random()*10);
						newchar=""+ran;
						boolean randredo =  Math.random()<0.90; //only 1 in 10 correct nums aare accepted

						if (newchar.equals(tar) && (randredo)){
							newchar="0";
						}
						newScoreVis=newScoreVis+newchar;
					}
				}
			
				//we replace the formular used to update
				CurrentlyDisplayedScore = Double.parseDouble(newScoreVis);
				
				//Note direction of count makes no sense on this mode, so we need to flag that.
				super.countHasNoDirection(true);
				
			}

		};
		
		//Pick random digit, +1 till correct
		//(does not reset/remove digits nicely)
		final GenericScoreDisplayer oneAtATime = new GenericScoreDisplayer(){
			@Override
			public void updateDisplayedScoreVar(float secs) {
				String newScoreVis = "";
				//current as string
				String curAsString =""+((int)CurrentlyDisplayedScore);
				//get tqarget length
				String targetASstring = ""+CurrentScore;
				int len = targetASstring.length();
				
				boolean changed=false;
				Log.info("changeing to:"+curAsString);
				//lefypad
				curAsString=Strings.padStart(curAsString, len, '0');
				
				//loop over digits
				for (int i = 0; i < len; i++) {
					String newchar = "";

					newchar=curAsString.substring(i, i+1);
					
					String tar = targetASstring.substring(i, i+1);
					//if its the one we want to change and its not already correct
					if ( (!newchar.equals(tar) ) && !changed){
						int asint = (int)Integer.parseInt(newchar);
						if (asint==9){
							asint = 0;
						} else {
							asint = (asint+1);
						}
						newchar=""+ (asint);
						changed=true;
					}
					newScoreVis=newScoreVis+newchar;
				}
				//we replace the formular used to update
				CurrentlyDisplayedScore = Double.parseDouble(newScoreVis);
				
				//Note direction of count makes no sense on this mode, so we need to flag that.
				super.countHasNoDirection(true);
				
				
			}

		};

		//starting scores
		fivesecondLerp.AddScore(777);
		darksStandard.AddScore(777);		
		constantDigits.AddScore(777);		
		randomDigits.AddScore(777);
		oneAtATime.AddScore(777);
		
		//set positions
		fivesecondLerp.setToPosition(new Vector3(-190,120,0));
		darksStandard.setToPosition(new Vector3(-190,70,0));	
		constantDigits.setToPosition(new Vector3(-190,20,0));
		randomDigits.setToPosition(new Vector3(-190,-30,0));
		oneAtATime.setToPosition(new Vector3(-190,-80,0));
		
		//give them a spiffy angle
		fivesecondLerp.setToRotation(new Quaternion(new Vector3(0, 1,0),65)); 
		darksStandard.setToRotation(new Quaternion(new Vector3(0, 1,0),65)); 
		constantDigits.setToRotation(new Quaternion(new Vector3(0, 1,0),65)); 		
		randomDigits.setToRotation(new Quaternion(new Vector3(0, 1,0),65)); 
		oneAtATime.setToRotation(new Quaternion(new Vector3(0, 1,0),65)); 

		//style them a bit
		fivesecondLerp.getStyle().setColor(Color.PURPLE);
		fivesecondLerp.getStyle().setTextGlowColor(Color.PURPLE);
		fivesecondLerp.getStyle().setTextGlowSize(5);
		
		darksStandard.getStyle().setColor(Color.RED);
		darksStandard.getStyle().setTextGlowColor(Color.RED);
		darksStandard.getStyle().setTextGlowSize(5);

		constantDigits.getStyle().setColor(Color.ORANGE);
		constantDigits.getStyle().setTextGlowColor(Color.ORANGE);
		constantDigits.getStyle().setTextGlowSize(5);

		randomDigits.getStyle().setColor(Color.YELLOW);
		randomDigits.getStyle().setTextGlowColor(Color.YELLOW);
		randomDigits.getStyle().setTextGlowSize(5);

		oneAtATime.getStyle().setColor(Color.BLUE);
		oneAtATime.getStyle().setTextGlowColor(Color.BLUE);
		oneAtATime.getStyle().setTextGlowSize(5);

		//add to scene
		GWTishModelManagement.addmodel(fivesecondLerp);	
		GWTishModelManagement.addmodel(darksStandard);		
		GWTishModelManagement.addmodel(constantDigits);		
		GWTishModelManagement.addmodel(randomDigits);		
		GWTishModelManagement.addmodel(oneAtATime);		


		//controlls
		Button addSmall = new Button("addSmall", new ClickHandler() {			
			@Override
			public void onClick() {
				int small = (int) (Math.random()*1000);
				fivesecondLerp.AddScore(small);	
				darksStandard.AddScore(small);	
				constantDigits.AddScore(small);	
				randomDigits.AddScore(small);		
				oneAtATime.AddScore(small);		
				
			}
		});
		Button addBig = new Button("addBig", new ClickHandler() {			
			@Override
			public void onClick() {
				int big = (int) (Math.random()*100000);
				fivesecondLerp.AddScore(big);
				darksStandard.AddScore(big);		
				constantDigits.AddScore(big);		
				randomDigits.AddScore(big);		
				oneAtATime.AddScore(big);	
				
			}
		});
		Button reset = new Button("reset", new ClickHandler() {			
			@Override
			public void onClick() {

				fivesecondLerp.SetScore(0);
				darksStandard.SetScore(0);
				constantDigits.SetScore(0);
				randomDigits.SetScore(0);
				oneAtATime.SetScore(0);

			}
		});
		//style
		addSmall.getCaption().getStyle().setPadding(6);
		addSmall.getCaption().getStyle().setFontSize(18, Unit.PX);
		addSmall.getCaption().getStyle().setColor(Color.RED);

		addBig.getCaption().getStyle().setStyleToMatch(addSmall.getCaption().getStyle(), false, false);
		reset.getCaption().getStyle().setStyleToMatch(addSmall.getCaption().getStyle(), false, false);
		//--




		addBig.setToPosition(new Vector3(-50,70,0));
		addSmall.setToPosition(new Vector3(-50,20,0));
		reset.setToPosition(new Vector3(-50,-30,0));


		GWTishModelManagement.addmodel(addBig);		
		GWTishModelManagement.addmodel(addSmall);		
		GWTishModelManagement.addmodel(reset);		

	}

	Environment environment;

	@Override
	public void render () {
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		float delta = Gdx.graphics.getDeltaTime(); //in seconds!
		GWTishModelManagement.updateTouchState();		


		//-----------
		float xc = Gdx.input.getX();
		float yc = Gdx.input.getY();
		Vector2 screenCursorPosition = new Vector2(xc,yc);	
		GWTishModelManagement.getHitables(screenCursorPosition.x,screenCursorPosition.y,cam);
		//----

	
		GWTishModelManagement.updateObjectMovementAndFrames(delta);


		GWTishModelManagement.modelBatch.begin(cam);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT); //We clear the depth buffer so all the overlays genuinely overlay everything
		GWTishModelManagement.modelBatch.render(GWTishModelManagement.allStandardInstances,environment); //basic environment
		GWTishModelManagement.modelBatch.render(GWTishModelManagement.allOverlayInstances,environment); //overlays
		GWTishModelManagement.modelBatch.end();

	}








	@Override
	public void dispose () {
	
		GWTishModelManagement.dispose();
	}

	//
	//Other gwtish/shader testing functions (ignore)
	//
	private static void addTestModels() {
	
		Log.info("adding test models..  ");
		Material blue = new Material
				(
						ColorAttribute.createSpecular(Color.WHITE),
						new BlendingAttribute(1f), 
						FloatAttribute.createShininess(16f),
						ColorAttribute.createDiffuse(Color.BLUE)
						);
	
	
		//	String testpng = "testpng.png";
		String testpng = "transtest.png";
		//	String testpng = "blue.png";
	
		FileHandle imageFileHandle = Gdx.files.internal(testpng);  //transtest.png
		Texture testtexture = new Texture(imageFileHandle);
	
	
		GwtishWidgetShaderAttribute gwtishWidgetShaderAttribute = new GwtishWidgetShaderAttribute(GwtishWidgetShaderAttribute.presetTextStyle.NULL_DONTRENDERTEXT);
		//gwtishWidgetShaderAttribute.filter_brightness = 1.512f;
		//gwtishWidgetShaderAttribute.checkShaderRequirements();
		gwtishWidgetShaderAttribute.backColor=Color.BLUE;
		gwtishWidgetShaderAttribute.backColor.a=0.75f;
	
		gwtishWidgetShaderAttribute.borderWidth=4f;
		gwtishWidgetShaderAttribute.cornerRadius=5f;
		gwtishWidgetShaderAttribute.borderColour=Color.RED;
		gwtishWidgetShaderAttribute.borderColour.a=0.5f;
		//		gwtishWidgetShaderAttribute.shadowColour=Color.BLACK;
		//		gwtishWidgetShaderAttribute.shadowBlur=0.5f;
		//		gwtishWidgetShaderAttribute.shadowXDisplacement=22.0f;
		//		gwtishWidgetShaderAttribute.shadowYDisplacement=22.0f;
	
		//gwtishWidgetShaderAttribute.filter_brightness=1.5f;
		//	gwtishWidgetShaderAttribute.filter_saturation=0.0f;
		//	gwtishWidgetShaderAttribute.filter_value=1.0f;
		//gwtishWidgetShaderAttribute.filter_hue=0.5f;
	
		gwtishWidgetShaderAttribute.checkShaderRequirements();
		//		
		Log.info("adding test models....  ");
		Material gwtish = new Material(
				"SHADERFORBACKGROUND",
				new BlendingAttribute(true,GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,1.0f),
				gwtishWidgetShaderAttribute,							 
				TextureAttribute.createDiffuse(testtexture)
				);
	
		ModelBuilder modelBuilder = new ModelBuilder();
	
		Model lookAtTesterm =  modelBuilder.createXYZCoordinates(195f, blue, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		ModelInstance lookAtTester = new ModelInstance(lookAtTesterm);
		lookAtTester.transform.setToTranslation(0, 0,0);
	
		//allStandardInstances.add(lookAtTester);
	
	
		Model lookAtTesterm2 =  modelBuilder.createXYZCoordinates(195f, blue, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		ModelInstance lookAtTester2 = new ModelInstance(lookAtTesterm2);
		lookAtTester2.transform.setToTranslation(50, 50, 50);
	
		//allStandardInstances.add(lookAtTester2);
		GWTishModelManagement.addmodel(lookAtTester2);		
	
		//Model model = modelBuilder.createBox(150f, 150f, 150f, gwtish,
		//            Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	
	
		Model model = modelBuilder.createRect(
				-120, 120, 0,
				-120, -120, 0,
				120, -120, 0, 
				120, 120, 0,
				0, 0, -1, 
				gwtish,				
				Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	
	
		Log.info("adding test models......  ");
	
		ModelInstance      instance = new ModelInstance(model);
	
		instance.transform.rotate(Vector3.Z, 90);
		instance.transform.translate(0, 0, 0);
	
		//GwtishWidgetShaderAttribute textStyleData = (GwtishWidgetShaderAttribute)instance.getMaterial("SHADERFORBACKGROUND").get(GwtishWidgetShaderAttribute.ID);
		//Gdx.app.log("shadertest:", "textStyleData:"+textStyleData.getDebugString());
	
	
		//allStandardInstances.add(instance);
		GWTishModelManagement.addmodel(instance);		
	
		Log.info("added test models  ");
	
	}

	private void textTests() {
		Label testlab = new Label("Red Shadow");    
		testlab.getStyle().setShadowColor(Color.RED);
	
		testlab.setToPosition(new Vector3(0,0,0));
		GWTishModelManagement.addmodel(testlab);		
	
		Label testlab2 = new Label("Blue Shadow");
		testlab2.getStyle().setShadowColor(Color.BLUE);
		testlab2.getStyle().setShadowBlur(0);
		testlab2.getStyle().setShadowX(-5);
		testlab2.getStyle().setShadowY(-5);
	
	
		testlab.setToPosition(new Vector3(0,-50,0));
		GWTishModelManagement.addmodel(testlab2);		
	
	
		Label testlab3 = new Label("Purple Glow+Text");
		testlab3.getStyle().setColor(Color.PURPLE);
		testlab3.getStyle().setTextGlowColor(Color.PURPLE);
		testlab3.getStyle().setTextGlowSize(0.9f);
		testlab3.getStyle().setFontSize(22, Unit.PX);
	
	
	
		testlab3.setToPosition(new Vector3(0,-100,0));
		GWTishModelManagement.addmodel(testlab3);
	
		Log.info("added label models  ");
	
	}
}
