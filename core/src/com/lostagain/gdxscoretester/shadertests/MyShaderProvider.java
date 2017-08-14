package com.lostagain.gdxscoretester.shadertests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;


/**
 * To help manage shader.
 * Stores custom attributes that can be used for object materials, these custom attributes will then trigger a specific shader
 * 
 * @author Tom
 *
 */
public class MyShaderProvider extends DefaultShaderProvider {
	public final DefaultShader.Config config;
	final static String logstag = "ME.MyShaderProvider";
	
	//known shaders
	static public enum shadertypes {		
		distancefieldfordataobjects
	}

	public MyShaderProvider (final DefaultShader.Config config) {
		this.config = (config == null) ? new DefaultShader.Config() : config;
	}

	public MyShaderProvider (final String vertexShader, final String fragmentShader) {
		this(new DefaultShader.Config(vertexShader, fragmentShader));
		
		
	}

	public MyShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	public MyShaderProvider () {
		this(null);
	}
	
	public void testListShader(Renderable instance){
		
		for (Shader shader : shaders) {
			
			Gdx.app.log(logstag, "shader="+shader.getClass().getName());
			
			Gdx.app.log(logstag, "can render="+shader.canRender(instance));
			
		}
	}
	
	@Override
	protected Shader createShader (final Renderable renderable) {
		
		//New method for selection (we should slowly move the things from the switch statement to this method)
		if (renderable.material.has(GwtishWidgetShaderAttribute.ID)){
			return new GwtishWidgetShader(renderable);
		}
				
		if (renderable.material.has(InvertShader.InvertAttribute.ID)){
			return new InvertShader(renderable);		
		}
				return new DefaultShader(renderable, new DefaultShader.Config());
		
	}
}