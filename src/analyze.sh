#!/bin/bash
export PATH=$CHECKERFRAMEWORK/checker/bin:$PATH

JARS="/homes/gws/csgordon/research/experiments/hudson-eclipse/lib/commons-logging-1.0.4.jar:/homes/gws/csgordon/research/experiments/hudson-eclipse/lib/commons-httpclient-3.1.jar:/homes/gws/csgordon/research/experiments/hudson-eclipse/lib/apache-log4j.jar:/homes/gws/csgordon/research/experiments/hudson-eclipse/lib/commons-codec-1.3.jar"

ECLIPSEJARS="/usr/lib/eclipse/plugins/org.eclipse.swt.gtk.linux.x86_64_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.ui_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.swt_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.jface_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.core.commands_3.6.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.ui.workbench_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.core.runtime_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.osgi_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.equinox.common_3.6.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.core.jobs_3.5.100.dist.jar:/usr/lib/ecliplse/plugins/org.eclipse.core.runtime.compatibility.registry_3.5.0.dist/runtime_registry_compatibility.jar:/usr/lib/eclipse/plugins/org.eclipse.equinox.registry_3.5.100.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.equinox.preferences_3.4.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.core.contenttype_3.4.100.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.equinox.app_1.3.100.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.ui.forms_3.5.100.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.ui.workbench.texteditor_3.7.0.dist.jar:/usr/lib/eclipse/plugins/org.eclipse.core.net_1.2.100.dist.jar"



DEBUG= #"-AprintErrorStack -Afilenames -Ashowchecks" #-Alint=debugSpew"
ERRS=999

COMMAND="javac -Xmaxerrs $ERRS -cp $JARS:$ECLIPSEJARS -processor org.checkerframework.checker.guieffect.GuiEffectChecker $DEBUG"

echo $COMMAND

find . -name '*.java' | xargs $COMMAND


#
