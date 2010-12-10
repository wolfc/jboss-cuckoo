/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.cuckoo.client;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jboss.cuckoo.common.io.Channels;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class Client
{
   private static void copy(InputStream input, OutputStream output) throws IOException
   {
      byte buf[] = new byte[8192];
      BufferedInputStream in = new BufferedInputStream(input, buf.length);
      BufferedOutputStream out = new BufferedOutputStream(output, buf.length);
      int l;
      while((l = in.read(buf)) >= 0)
         out.write(buf, 0, l);
      out.flush();
   }

   public static void main(String args[]) throws Exception
   {
      // connect to cuckoo server
      Socket socket = new Socket("localhost", 45451);

      String hostname = InetAddress.getLocalHost().getHostName();

      InetSocketAddress address = new InetSocketAddress(hostname, 0);
      int backlog = 5;
      HttpServer server = HttpServer.create(address, backlog);
      HttpHandler handler = new HttpHandler()
      {
         @Override
         public void handle(HttpExchange httpExchange) throws IOException
         {
            try
            {
               if(!httpExchange.getRequestMethod().equals("GET"))
                  throw new IllegalArgumentException("Only GET is supported");
               String resource = httpExchange.getRequestURI().getPath();
               ClassLoader cl = Thread.currentThread().getContextClassLoader();
               URL url = cl.getResource(resource.substring(1));
               if(url == null)
               {
                  System.err.println("Can't find resource " + resource + " in " + cl);
                  httpExchange.sendResponseHeaders(404, 0);
                  return;
               }
               InputStream in = url.openStream();
               httpExchange.sendResponseHeaders(200, 0);
               copy(in, httpExchange.getResponseBody());
            }
            catch(Exception e)
            {
               httpExchange.sendResponseHeaders(500, 0);
               e.printStackTrace(new PrintStream(httpExchange.getResponseBody()));
            }
            finally
            {
               httpExchange.close();
            }
         }
      };
      HttpContext context = server.createContext("/", handler);
      server.start();

      try
      {
         int port = server.getAddress().getPort();

         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

         writer.write("E http://" + hostname + ":" + port + "/ " + args[0]);
         writer.newLine();
         writer.flush();

         ObjectInput in = new ObjectInputStream(socket.getInputStream());
         int ch;
         while((ch = in.read()) != -1)
         {
            Object obj = in.readObject();
            // ahem
            byte b[] = (byte[]) obj;
            switch(ch)
            {
               case Channels.OUT:
                  System.out.write(b);
                  System.out.flush();
                  break;
               case Channels.ERR:
                  System.err.write(b);
                  System.err.flush();
                  break;
            }
         }
      }
      finally
      {
         server.stop(0);
      }
   }
}
