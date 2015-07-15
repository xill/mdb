package com.xill.mangadb.control;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * thanks to "https://github.com/NetsOSS/embedded-jetty" for this awesome hack.
 * 
 * This is a hack to get the default servlet to not use file mapping buffers when serving files. The effect is that
 * Jetty reloads the file on every read but it also does not lock the file which is good for development.
 */
// FIXME : stopped working for some reason...
public class DisableFileMappedBufferConfiguration extends AbstractConfiguration {
    @Override
    public void configure(WebAppContext context) throws Exception {
        ServletContextHandler.Decorator useFileMappedBuffer = new ServletContextHandler.Decorator() {
            @SuppressWarnings({ "unchecked", "serial" })
			@Override
            public <T> T decorate(T o) {
                if (o instanceof DefaultServlet) {
                    Class<T> klass = (Class<T>) o.getClass();

                    return klass.cast(new DefaultServlet() {
                        @Override
                        public String getInitParameter(String name) {
                            if (name.equals("useFileMappedBuffer")) {
                                return "false";
                            }

                            return super.getInitParameter(name);
                        }
                    });
                }
                return o;
            }

            @Override
            public void destroy(Object o) {
            }
        };

        context.addDecorator(useFileMappedBuffer);
    }
}