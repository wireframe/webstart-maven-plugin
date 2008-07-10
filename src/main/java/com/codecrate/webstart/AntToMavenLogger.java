/*
 * Copyright 2007 Codecrate
 *
 * Licensed under the Apache License, Version 2.0 (the "License" );
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codecrate.webstart;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;

public class AntToMavenLogger implements BuildListener {
    private final AbstractMojo mojo;

    public AntToMavenLogger(AbstractMojo mojo) {
        this.mojo = mojo;
    }

    public void buildStarted(BuildEvent event) {
        log(event);
    }

    public void buildFinished(BuildEvent event) {
        log(event);
    }

    public void targetStarted(BuildEvent event) {
        log(event);
    }

    public void targetFinished(BuildEvent event) {
        log(event);
    }

    public void taskStarted(BuildEvent event) {
        log(event);
    }

    public void taskFinished(BuildEvent event) {
        log(event);
    }

    public void messageLogged(BuildEvent event) {
        log(event);
    }

    private void log(BuildEvent event) {
        int priority = event.getPriority();
        Log log = mojo.getLog();
        switch (priority) {
        case Project.MSG_ERR:
            log.error(event.getMessage());
            break;

        case Project.MSG_WARN:
            log.warn(event.getMessage());
            break;

        case Project.MSG_INFO:
            log.info(event.getMessage());
            break;

        case Project.MSG_VERBOSE:
            log.debug(event.getMessage());
            break;

        case Project.MSG_DEBUG:
            log.debug(event.getMessage());
            break;

        default:
            log.info(event.getMessage());
        break;
        }
    }
}