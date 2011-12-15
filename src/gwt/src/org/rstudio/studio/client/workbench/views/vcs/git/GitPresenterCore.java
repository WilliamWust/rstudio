/*
 * GitPresenterCore.java
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.vcs.git;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.rstudio.core.client.command.CommandBinder;
import org.rstudio.core.client.command.Handler;
import org.rstudio.studio.client.application.events.EventBus;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.SimpleRequestCallback;
import org.rstudio.studio.client.common.console.ConsoleProcess;
import org.rstudio.studio.client.common.satellite.Satellite;
import org.rstudio.studio.client.common.satellite.SatelliteManager;
import org.rstudio.studio.client.common.vcs.GitServerOperations;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.views.vcs.common.ConsoleProgressDialog;
import org.rstudio.studio.client.workbench.views.vcs.common.events.VcsRefreshEvent;
import org.rstudio.studio.client.workbench.views.vcs.common.events.VcsRefreshHandler;
import org.rstudio.studio.client.workbench.views.vcs.git.model.GitState;

@Singleton
public class GitPresenterCore
{
   public interface Binder extends CommandBinder<Commands, GitPresenterCore> {}
   
   @Inject
   public GitPresenterCore(GitServerOperations server,
                           GitState gitState,
                           final Commands commands,
                           Binder commandBinder,
                           EventBus eventBus,
                           final GlobalDisplay globalDisplay,
                           final Satellite satellite,
                           final SatelliteManager satelliteManager)
   {
      server_ = server;
      gitState_ = gitState;
      
      commandBinder.bind(commands, this);

      gitState_.addVcsRefreshHandler(new VcsRefreshHandler()
      {
         @Override
         public void onVcsRefresh(VcsRefreshEvent event)
         {
            boolean hasRemote = gitState_.hasRemote();
            commands.vcsPull().setEnabled(hasRemote);
            commands.vcsPush().setEnabled(hasRemote);
         }
      });
   }

   @Handler
   void onVcsRefresh()
   {
      gitState_.refresh();
   }

   
   public void onVcsPull()
   {
      server_.gitPull(new SimpleRequestCallback<ConsoleProcess>()
      {
         @Override
         public void onResponseReceived(ConsoleProcess proc)
         {
            new ConsoleProgressDialog(proc, server_).showModal();
         }
      });
   }

   public void onVcsPush()
   {
      server_.gitPush(new SimpleRequestCallback<ConsoleProcess>()
      {
         @Override
         public void onResponseReceived(ConsoleProcess proc)
         {
            new ConsoleProgressDialog(proc, server_).showModal();
         }
      });
   }
    
   private final GitServerOperations server_;
   private final GitState gitState_;
}
