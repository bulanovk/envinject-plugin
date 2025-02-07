package org.jenkinsci.plugins.envinject.service;

import hudson.EnvVars;
import hudson.Main;
import hudson.Platform;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.lib.envinject.EnvInjectException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Gregory Boissinot
 */
public class EnvInjectMasterEnvVarsSetter extends MasterToSlaveCallable<Void, EnvInjectException> {

    private @NonNull EnvVars enVars;

    public EnvInjectMasterEnvVarsSetter(@NonNull EnvVars enVars) {
        this.enVars = enVars;
    }

    @Override
    public Void call() throws EnvInjectException {
        try {
            Field platformField = EnvVars.class.getDeclaredField("platform");
            platformField.setAccessible(true);
            platformField.set(enVars, Platform.current());
            if (Main.isUnitTest || Main.isDevelopmentMode) {
                enVars.remove("MAVEN_OPTS");
            }
            Field masterEnvVarsFiled = EnvVars.class.getDeclaredField("masterEnvVars");
            masterEnvVarsFiled.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(masterEnvVarsFiled, masterEnvVarsFiled.getModifiers() & ~Modifier.FINAL);
            masterEnvVarsFiled.set(null, enVars);
        } catch (IllegalAccessException iae) {
            throw new EnvInjectException(iae);
        } catch (NoSuchFieldException nsfe) {
            throw new EnvInjectException(nsfe);
        }

        return null;
    }

}
