victims-plugin-eclipse
======================

Victims plugin for eclipse.

Contained in this repo are 2 projects, victims-plugin-eclipse and
victims-p2-repo. victims-p2-repo resolves maven dependencies for 
the eclipse plugin and converts them into OSGi bundles in a 
p2 compliant repository. The victims-p2-repo contains a plugin
which can host these locally on a webserver so they can be
accessed from the eclipse plugin using tycho.

For development you will require the Eclipse Tycho maven extension for
m2e and the Eclipse plugin development plugin. You should be able
to find these in the default eclipse update sites.
To set up for development you need to set up an eclipse workspace
with both the victims-p2-repo and victims-plugin-eclipse. You can
run the victims-p2-repo with the following maven goals:

  p2:site       - Downloads specified dependencies and creates the repo
  jetty:run     - Runs the webserver

After running the webserver you will need to install the new
software repository in Eclipse. Help -> Install New Software
-> Add
  Name: localrepo
  Location: http://localhost:8080/site/
Complete adding the software site and then execute maven in the
victims-plugin-eclipse project with the following goal:

  clean install

You should then be able to right click on the victims-plugin-eclipse
project and then Run As -> Eclipse Application

If all went well you are now running Eclipse with victims-plugin-eclipse
running inside it. You can add a project and right click on it to find
the Victims Scan option.
