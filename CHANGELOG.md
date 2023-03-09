5.7.1 (March 2023)
------------------

- Push to releases folder on artifactory([#73](https://github.com/ome/omero-gateway-java/pull/73))
- Fix warning in javadoc ([#70](https://github.com/ome/omero-gateway-java/pull/70))
- Bump omero-blitz version to 5.6.1

5.7.0 (December 2022)
---------------------

- Remove joinsession ([#63](https://github.com/ome/omero-gateway-java/pull/63))
- Bump to TestNG 7.5 ([#61](https://github.com/ome/omero-gateway-java/pull/61))
- Bump omero-blitz version to 5.6.0


5.6.10 (June 2022)
------------------

- Bump omero-blitz version to 5.5.12
- Bump org.openmicroscopy.project plugin to 5.5.4
- Add Gradle publication workflow

5.6.9 (April 2022)
------------------

- Bump omero-blitz version to 5.5.10

5.6.8 (April 2022)
------------------

- Bump omero-blitz version to 5.5.9

5.6.7 (September 2021)
----------------------

- Fix tablesfacility ([#58](https://github.com/ome/omero-gateway-java/pull/58))
- Add method to get PlaneInfos ([#57](https://github.com/ome/omero-gateway-java/pull/57))
- Add resolution parameter to getTile method ([#51](https://github.com/ome/omero-gateway-java/pull/51))
- Handle well column ([#52](https://github.com/ome/omero-gateway-java/pull/52))

5.6.6 (June 2021)
-----------------

- Fallback if args login doesn't work ([#53](https://github.com/ome/omero-gateway-java/pull/53))

5.6.5 (September 2020)
----------------------

- Bump omero-blitz version to 5.5.8

5.6.4 (July 2020)
-----------------

- Improve error message ([#34](https://github.com/ome/omero-gateway-java/pull/34))
- Note the client IP address when connecting to the server ([#36](https://github.com/ome/omero-gateway-java/pull/36))
- Add missing license file ([#37](https://github.com/ome/omero-gateway-java/pull/37))
- Bump omero-blitz version to 5.5.7

5.6.3 (March 2020)
------------------

- Set default port for connecting to server via websockets
  ([#28](https://github.com/ome/omero-gateway-java/pull/28))
- Migrate unit tests and display their output instead of caching it
  ([#31](https://github.com/ome/omero-gateway-java/pull/31))
- Bump TestNG version to 6.14.2
  ([#32](https://github.com/ome/omero-gateway-java/pull/32))
- Bump omero-blitz version to 5.5.6

5.6.2 (December 2019)
---------------------

- Bump omero-blitz version to 5.5.5

5.6.1 (December 2019)
---------------------

- Catch all length conversion failures and return null ([#26](https://github.com/ome/omero-gateway-java/pull/26))

5.6.0 (December 2019)
---------------------

- Allow connecting to 5.5 & 5.6 servers ([#23](https://github.com/ome/omero-gateway-java/pull/23))
- Enable connections to web sockets ([#21](https://github.com/ome/omero-gateway-java/pull/21))
- Add method to TableFacility ([#18](https://github.com/ome/omero-gateway-java/pull/18))
- Re-correct handling of infinite pixel sizes ([#15](https://github.com/ome/omero-gateway-java/pull/15))

5.5.4 (August 2019)
-------------------

- Prevent import of large number of files to hang ([#16](https://github.com/ome/omero-gateway-java/pull/16))

5.5.3 (July 2019)
-----------------

- Bump omero-blitz version fixing SSL issues
- Chgrp fixes ([#10](https://github.com/ome/omero-gateway-java/pull/10))
- Don't close sessions opened by UUID ([#11](https://github.com/ome/omero-gateway-java/pull/11))
- Add new gateway methods ([#12](https://github.com/ome/omero-gateway-java/pull/12), [#13](https://github.com/ome/omero-gateway-java/pull/12))
- Correct handling of infinite pixel sizes ([#14](https://github.com/ome/omero-gateway-java/pull/14))

5.5.2 (June 2019)
-----------------

- Bump omero-blitz version.

5.5.1 (May 2019)
----------------

- Replace usage of deprecated misspelled method.
- Update omero-blitz dependency version.

5.5.0 (May 2019)
----------------

- Exclude server dependencies.
- Get version from package.
- Add omero-blitz dependency as api.
- Extract Java Gateway from omero-blitz.
