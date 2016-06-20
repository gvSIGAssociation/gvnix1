#!/bin/bash
#
# Script to generate gvNIX release

# TODO: Comprobar versión de JDK (1.6+) y MVN (3.0+)

## Exit script on any error
set -e 

## Get current file abs_path
function abs_path() {
    pushd . > /dev/null;
    if [ -d "$1" ]; then
      cd "$1";
      dirs -l +0;
    else
      cd "`dirname \"$1\"`";
      cur_dir=`dirs -l +0`;
      if [ "$cur_dir" == "/" ]; then
        echo "$cur_dir`basename \"$1\"`";
      else
        echo "$cur_dir/`basename \"$1\"`";
     fi;
   fi;
   popd > /dev/null;
}
THIS_FILE=$(abs_path $0)


GVNIX_DEPLOYMENT_SUPPORT_DIR=`dirname $THIS_FILE`
GVNIX_HOME=`dirname $GVNIX_DEPLOYMENT_SUPPORT_DIR`
ROO_HOME=$GVNIX_HOME/roo

## Load utils functions
source $GVNIX_DEPLOYMENT_SUPPORT_DIR/build-util.functions

# Gets gvNIX version from first version tag on root pom.xml
GVNIX_VERSION=`grep "[<]version[>]\K([^<]*)" $GVNIX_HOME/pom.xml -oPm1`
# Gets roo version from first version tag on root pom.xml
ROO_VERSION=`grep "[<]version[>]\K([^<]*)" $ROO_HOME/pom.xml -oPm1`


function usage () {
cat << EOF

Usage: $0 [package|test|deploy] {--skipCleanRepo} {--skipDoc} {--simple} {--tomcatPort} {-d | --debug}

Actions:

    package     
              Generates gvNIX package

    test        
              Generates gvNIX package and runs CI test (skip documentation)

    deploy    
              Generates gvNIX package, runs CI test and deploy all ".jar" 
              artifacts on Maven Central

Options:

    --skipCleanRepo

              Skip cleaning of Maven local repository (~/.m2/repository)
              of any gvNIX and Spring Roo artifacts

    --skipDoc

              Skip documentation generation. Only handle for "package" and "test" action. 

    --simple

              Ommit Roo test and run gvNIX as fast as posible. This options includes
              --skipCleanRepo and --skipDoc. Also skips roo clean, compilation and
              integrations test. Only handle for "package" and "test"

    --tomcatPort
            
            Tomcat port to use when execute CI tests. Default 8080

    --generateDistRepo

            Generates maven repository folder on "$HOME/gvnix-dist-repo" that includes all 
            maven dependencies of current gvNIX version and Spring Roo version. 
            If this param is specified, --skipCleanRepo param will be ignored.

    -d,--debug

              Show executed commands

Description:

    Creates the release ZIP. Automates building deployment ZIPs 
    and deployment, runs CI test and deploy ".jar" artifacts 
    on Maven Central.

EOF

}


if [ -z "$1" ]; then
  usage
  exit 1
fi

## Prepare configuration variables
SKIP_LOCAL_REPO_CLEAN=no
SKIP_ROO_COMPILE=no
SKIP_ROO_INTEGRATION=no
GENERATE_DOC=yes
RUN_CI=no
DEPLOY_JARS=no
DEBUG=no
TOMCAT_PORT=8080
MAVEN_LOCAL_REPO=~/.m2/repository
GENERATE_DIST_REPO=no

## identify action and options
ACTION="$1"
shift

while [[ $# > 0 ]]
do
  OPTION="$1"
    case $OPTION in
        --skipCleanRepo)
            SKIP_LOCAL_REPO_CLEAN=yes
            ;;
        --skipDoc)
            GENERATE_DOC=no
            ;;
        --simple)
            GENERATE_DOC=no
            SKIP_LOCAL_REPO_CLEAN=yes
            SKIP_ROO_INTEGRATION=yes
            SKIP_ROO_COMPILE=yes
            ;;
        --tomcatPort)
            ## getting tomcat port
            shift
            TOMCAT_PORT=$1            
            ;;
        --generateDistRepo)
            MAVEN_LOCAL_REPO=~/gvnix-dist-repo/repository
            GENERATE_DIST_REPO=yes
            ;;
        -d|--debug)
            DEBUG=yes
            ;;
        *)
            echo ""
            echo "*** Invalid option: $OPTION"
            echo ""
            usage
            exit 1
            ;;
    esac
    ## next option
    shift 
done



case $ACTION in
package)
    RUN_CI=no
    DEPLOY_JARS=no
  ;;
test)
    RUN_CI=yes
    DEPLOY_JARS=no
  ;;
deploy)
    GENERATE_DOC=yes
    RUN_CI=yes
    DEPLOY_JARS=yes
    SKIP_ROO_COMPILE=no
    SKIP_ROO_INTEGRATION=no
    echo ""
    echo "**********************************************************"
    echo "**********************************************************"
    echo "***                                                 ******"
    echo "*** WARNING: deploy action is not full testest!!!!  ******"
    echo "***                                                 ******"
    echo "**********************************************************"
    echo "**********************************************************"
    echo ""
    echo "Press <Ctrl>+C to cancel or <Enter> to continue..."
    read 
  ;;
*)
    echo ""
    echo "*** Invalid action: $ACTION"
    echo ""
    usage
    exit 1
  ;;
esac


## Echo all commands
if [ "$DEBUG" = "yes" ]; then
    set -x 
fi


if [ "$GENERATE_DIST_REPO" = "yes" ]; then
    # Set SKIP_LOCAL_REPO_CLEAN to no if maven repo will be located on ~/gvnix-dist-repo/repository
    SKIP_LOCAL_REPO_CLEAN=no
    if [ -d $HOME/gvnix-dist-repo ]; then
      rm -r $HOME/gvnix-dist-repo
    fi  
    mkdir $HOME/gvnix-dist-repo
    mkdir $HOME/gvnix-dist-repo/repository
fi

if [ "$SKIP_LOCAL_REPO_CLEAN" = "yes" ]; then
    # Skip remove old roo and gvNIX installed dependencies
    show_message_info "Clean Maven local repository: Skip"
else
    # Remove old roo and gvNIX installed dependencies
    show_message_info "Clean Maven local repository ($MAVEN_LOCAL_REPO) of Spring Roo and gvNIX artifacts"
    rm -rf $MAVEN_LOCAL_REPO/org/springframework/roo  || true
    rm -rf $MAVEN_LOCAL_REPO/org/gvnix  || true
fi

### ROO PACKAGE

# Build roo
cd $ROO_HOME
if [ ! -d $MAVEN_LOCAL_REPO/org/springframework/roo ]; then
    ## force roo compilation 
    SKIP_ROO_COMPILE=no
fi
if [ ! -d "$ROO_HOME/target/all" ]; then
    ## force roo compilation 
    SKIP_ROO_COMPILE=no
fi


if [ "$SKIP_ROO_COMPILE" = "yes" ]; then
    show_message_info "Compilling and install Roo: Skip"
else 
    show_message_info "Compilling and install Roo"
    mvn -Dmaven.repo.local=$MAVEN_LOCAL_REPO clean install
fi

# Install and package modules
show_message_info "Compilling and install gvNIX"
cd $GVNIX_HOME
mvn -Dmaven.repo.local=$MAVEN_LOCAL_REPO clean install

# Copy gvNIX build modules (except support, already included on each add-on) together with Roo build modules
rm -rf "$GVNIX_HOME/target/all/org.gvnix.support*"
cp $GVNIX_HOME/target/all/org.gvnix.* $ROO_HOME/target/all

## Handle Spring Roo documentation
cd $ROO_HOME/deployment-support
mvn clean
if [ "$GENERATE_DOC" = "yes" ]; then
    show_message_info "Generating Roo documentation"
    mvn -Dmaven.repo.local=$MAVEN_LOCAL_REPO site
    show_message_info "Generating gvNIX documentation"
    cd $GVNIX_HOME
    mvn -Dmaven.repo.local=$MAVEN_LOCAL_REPO site
else
    show_message_info "Skip Roo documentation"
    # create a empty docs file to simulate that "mvn site" has been run
    mkdir -p target/site/reference/pdf/
    touch target/site/reference/pdf/spring-roo-docs.pdf
fi

# Remove old release
show_message_info "Clean old temporal folders"
rm -rf /tmp/gvNIX* || true
rm -rf /tmp/spring-roo-* || true

# Excecute roo package process
cd $ROO_HOME/deployment-support
if [ "$RUN_CI" = "yes" ] && [ ! "$SKIP_ROO_INTEGRATION" = "yes" ] ; then

  show_message_info "execute Roo package proccess with test"
  ./roo-deploy-dist.sh -c assembly -tv

else 

  show_message_info "execute Roo package proccess without test"
  ./roo-deploy-dist.sh -c assembly

fi

show_message_info "Generating gvNIX package"
# Unzip new release
unzip $ROO_HOME/target/roo-deploy/dist/spring-roo-$ROO_VERSION.zip -d /tmp
WORK_DIR="/tmp/spring-roo-$ROO_VERSION"
cd $GVNIX_HOME

# Add gvNIX start scripts copying roo scripts
cp $WORK_DIR/bin/roo.sh $WORK_DIR/bin/gvnix.sh
cp $WORK_DIR/bin/roo.bat $WORK_DIR/bin/gvnix.bat

# Copy static gvNIX resources (readme, license, samples) into Roo
cp $GVNIX_HOME/src/main/assembly/readme.txt $WORK_DIR/readme_gvNIX.txt
cp $GVNIX_HOME/LICENSE.TXT $WORK_DIR/LICENSE_gvNIX.TXT
cp $GVNIX_HOME/src/main/resources/*.roo $WORK_DIR/samples

# Generating Spring Roo docs folder and gvNIX docs folder
if [ "$GENERATE_DOC" = "yes" ]; then  
  mkdir $WORK_DIR/docs/spring-roo
  mv $WORK_DIR/docs/html/ $WORK_DIR/docs/spring-roo/
  mv $WORK_DIR/docs/pdf/ $WORK_DIR/docs/spring-roo/
  mkdir $WORK_DIR/docs/gvNIX
  cp -r $GVNIX_HOME/target/site/reference/* $WORK_DIR/docs/gvNIX
else
  show_message_info "Include gvNIX Documents: Skip"
fi


# Rename release to gvNIX, pack it and add installed release to path
GVNIX_WORK_DIR=/tmp/gvNIX-$GVNIX_VERSION
mv $WORK_DIR $GVNIX_WORK_DIR
cd /tmp
zip -9 -r gvNIX-$GVNIX_VERSION.zip gvNIX-$GVNIX_VERSION
export PATH=$GVNIX_WORK_DIR/bin:$PATH

# Copy the release to target dir (avoid to lost it when shutdown)
cd -
if [ ! -d "$GVNIX_HOME/target/gvnix-dist" ]; then
  mkdir -p target/gvnix-dist
fi
cp /tmp/gvNIX-$GVNIX_VERSION.zip target/gvnix-dist/

# Generate zip file with downloaded maven dependencies in
# target dir (avoid to lost it when shutdown)
if [ "$GENERATE_DIST_REPO" = "yes" ] && [ -d "$HOME/gvnix-dist-repo" ]; then
  zip -9 -r target/gvnix-dist/gvNIX-$GVNIX_VERSION-repository.zip ~/gvnix-dist-repo
fi

# Run gvNIX integration test
if [ "$RUN_CI" = "yes" ]; then
    show_message_info "Run Integration test"
    bash $GVNIX_DEPLOYMENT_SUPPORT_DIR/gvNIX-CI.sh /tmp/gvnix_int_test $GVNIX_WORK_DIR/bin/gvnix.sh $GVNIX_HOME $TOMCAT_PORT $MAVEN_LOCAL_REPO
else
    show_message_info "Run Integration test: Skip"
fi

# Deploy jars
if [ "$DEPLOY_JARS" = "yes" ]; then
    show_message_info "Deploy gvNIX Jars"
    cd $GVNIX_HOME
    mvn -Dmaven.repo.local=$MAVEN_LOCAL_REPO deploy
else
    show_message_info "Deploy gvNIX Jars: Skip"
fi



# Copy the release to target dir (avoid to lost it when shutdown)
cd -
mkdir -p target/gvnix-dist
cp /tmp/gvNIX-$GVNIX_VERSION.zip target/gvnix-dist/

# Info messages
echo ""
echo ""
show_message_info " Build process: SUCCESS"
echo ""
echo "gvNIX release created: $GVNIX_HOME/target/gvnix-dist/gvNIX-$GVNIX_VERSION.zip"

if [ "$GENERATE_DIST_REPO" = "yes" ]; then
  echo "gvNIX local maven repository created: $HOME/gvnix-dist-repo"
fi

echo "gvNIX installed on /tmp and setted on PATH for test purpouses."
echo "Type gvnix.sh to start"
