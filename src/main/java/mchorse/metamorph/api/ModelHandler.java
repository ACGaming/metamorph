package mchorse.metamorph.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class responsible for storing domain custom models and sending models to
 * players who are logged in.
 */
public class ModelHandler
{
    /**
     * Cached models, they're loaded from stuffs
     */
    public Map<String, Model> models = new HashMap<String, Model>();

    /**
     * Load a custom model with name and lowercase'd filename generated from 
     * name. 
     */
    public void load(String name)
    {
        this.load(name, name.toLowerCase());
    }

    /**
     * Load a custom model with name and lowercase'd filename generated from 
     * name. 
     */
    public void load(String name, String filename)
    {
        String path = "assets/metamorph/models/entity/";
        ClassLoader loader = this.getClass().getClassLoader();

        this.load(name, loader.getResourceAsStream(path + filename + ".json"));
    }

    /**
     * Load a custom model with name and filename
     */
    public void load(String name, InputStream stream)
    {
        try
        {
            this.models.put(name, Model.parse(stream));
        }
        catch (Exception e)
        {
            System.out.println("Failed to load a custom model by name '" + name + "'");

            e.printStackTrace();
        }
    }
}