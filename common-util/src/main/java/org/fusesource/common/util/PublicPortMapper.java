package org.fusesource.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Use to handle mapping local ports to public ports that clients
 * can connect to.  The default implementation just assumes that the local
 * port is the public port.  Set the "org.fusesource.PortMapper" system property
 * to a class name that implements that PublicPortMapper.SPI to use a different
 * implementation.
 * For example: -Dorg.fusesource.common.util.PublicPortMapper=org.example.MySPI
 */
abstract public class PublicPortMapper {

    abstract static public class SPI {
        abstract int getPublicPort(int localPort);
    }

    static public class DefaultSPI extends SPI {
        @Override
        int getPublicPort(int localPort) {
            return localPort;
        }
    }

    static public class OpenShiftSPI extends SPI {

        HashMap<Integer,Integer> ports = new HashMap<Integer,Integer>();

        public OpenShiftSPI() {
            Map<String, String> envs = System.getenv();
            for (Map.Entry<String, String> entry : envs.entrySet()) {
                String key = entry.getKey();
                String publicPort = entry.getValue();
                if( key.startsWith("OPENSHIFT_") && key.endsWith("_PROXY_PORT") ) {
                    String prefix = Strings.stripSuffix(key, "_PROXY_PORT");
                    String privatePort = envs.get(prefix+"_PORT");
                    if( privatePort!=null ) {
                        try {
                            ports.put(new Integer(privatePort), new Integer(publicPort) );
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
            }
        }


        @Override
        int getPublicPort(int localPort) {
            Integer rc = ports.get(localPort);
            if( rc != null ) {
                return rc.intValue();
            }
            return localPort;
        }
    }

    private static SPI create() {
        Class mapperClass = DefaultSPI.class;
        String mapperClassName = System.getProperty(PublicPortMapper.class.getName());
        try {
            if( mapperClassName!=null ) {
                mapperClassName = mapperClassName.trim();
                if( mapperClassName.equals("default") ) {
                    mapperClassName = DefaultSPI.class.getName();
                }
                if( mapperClassName.equals("openshift") ) {
                    mapperClassName = OpenShiftSPI.class.getName();
                }
                try {
                    mapperClass = PublicPortMapper.class.getClassLoader().loadClass(mapperClassName);
                } catch (ClassNotFoundException e) {
                    mapperClass = Thread.currentThread().getContextClassLoader().loadClass(mapperClassName);
                }
            }
            return (SPI) mapperClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //  todo.. if perf is a problem we should perhpas cache the SPI impl.
    //  private static SPI INSTANCE = create();

    static public int getPublicPort(int localPort) {
        return create().getPublicPort(localPort);
    }

}
