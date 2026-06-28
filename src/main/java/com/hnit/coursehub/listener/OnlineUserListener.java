package com.hnit.coursehub.listener;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class OnlineUserListener implements HttpSessionListener {
    private static final String ONLINE_COUNT = "onlineCount";

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        ServletContext context = se.getSession().getServletContext();
        AtomicInteger count = getCounter(context);
        context.setAttribute(ONLINE_COUNT, count.incrementAndGet());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        ServletContext context = se.getSession().getServletContext();
        AtomicInteger count = getCounter(context);
        int value = Math.max(0, count.decrementAndGet());
        context.setAttribute(ONLINE_COUNT, value);
    }

    private AtomicInteger getCounter(ServletContext context) {
        synchronized (context) {
            AtomicInteger counter = (AtomicInteger) context.getAttribute("onlineCounter");
            if (counter == null) {
                counter = new AtomicInteger(0);
                context.setAttribute("onlineCounter", counter);
            }
            return counter;
        }
    }
}
