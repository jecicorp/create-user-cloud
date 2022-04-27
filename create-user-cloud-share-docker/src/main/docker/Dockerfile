FROM ${docker.share.image}:${docker.share.version}

ARG TOMCAT_DIR=/usr/local/tomcat

WORKDIR $TOMCAT_DIR/amps_share/

# Ajout des modules
#  - support-tools OOTB
#  - share-site-creators
RUN set -x \
 && curl -sSLO "https://repo1.maven.org/maven2/org/orderofthebee/support-tools/support-tools-share/1.1.0.0/support-tools-share-1.1.0.0-amp.amp" \
 && curl -sSLO "https://github.com/jpotts/share-site-creators/releases/download/0.0.7/share-site-creators-share-0.0.7.amp" \
 && find ${TOMCAT_DIR}/webapps/share -type d -print0 | xargs -0 chmod 755 \
 && java -jar ${TOMCAT_DIR}/alfresco-mmt/alfresco-mmt-6.0.jar \
    install ${TOMCAT_DIR}/amps_share/ ${TOMCAT_DIR}/webapps/share \
    -nobackup -directory -force

COPY share-config-custom.xml $TOMCAT_DIR/shared/classes/alfresco/web-extension

COPY log4j.properties $TOMCAT_DIR/webapps/share/WEB-INF/classes

WORKDIR $TOMCAT_DIR