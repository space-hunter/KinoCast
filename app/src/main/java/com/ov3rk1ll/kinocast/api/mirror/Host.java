package com.ov3rk1ll.kinocast.api.mirror;

import com.ov3rk1ll.kinocast.ui.DetailActivity;
import com.ov3rk1ll.kinocast.utils.Utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.InvocationTargetException;

public abstract class Host {
    protected int mirror;
    protected String url;
    protected String slug;

    public static boolean DisableSSLCheck = false;

    public static Class<?>[] HOSTER_LIST = {
            DivxStage.class,
            NowVideo.class,
            SharedSx.class,
            Sockshare.class,
            StreamCloud.class,
            Vodlocker.class,
            StreamCherry.class,
            Streamango.class,
            Vidoza.class,
            VShare.class,
            Vidlox.class,
            Direct.class
    };

    public static Host selectById(int id) {
        for (Class<?> h : HOSTER_LIST) {
            try {
                Host host = (Host) h.getConstructor().newInstance();
                if (host.getId() == id) {
                    return host;
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Host() {

    }

    public Host(int mirror) {
        this.mirror = mirror;
    }

    public boolean isEnabled() {
        return false;
    }

    public abstract int getId();

    public abstract String getName();

    public int getMirror() {
        return mirror;
    }

    public void setMirror(int mirror) {
        this.mirror = mirror;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
    public String getVideoPath(DetailActivity.QueryPlayTask queryTask) {
        return null;
    }

    public String getMirrorLink(Document doc) {
        try {
            String href = null;
            Elements elem = doc.select("iframe");
            if (elem != null) {
                href = elem.attr("src");
            }
            if (Utils.isStringEmpty(href)) {
                elem = doc.select("a");
                if (elem != null) {
                    href = elem.attr("href");
                }
            }
            return Utils.getUrl(href);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return getName() + " #" + mirror;
    }

    public static Connection buildJsoup(String url) {
        return Jsoup.connect(url)
                .validateTLSCertificates(!DisableSSLCheck)
                .userAgent(Utils.USER_AGENT)
                .timeout(3000);
    }
}
