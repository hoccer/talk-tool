package com.hoccer.talk.tool;

import better.cli.CLIContext;
import better.cli.console.Console;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.client.HttpClientWithKeystore;
import com.hoccer.talk.client.XoClientConfiguration;
import com.hoccer.talk.tool.client.TalkToolClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TalkToolContext extends CLIContext {

    private static KeyStore KEYSTORE = null;

    TalkTool mApplication;
    ObjectMapper mMapper;
    ScheduledExecutorService mExecutor;
    AtomicInteger mClientIdCounter;
    List<TalkToolClient> mClients;
    Hashtable<Integer, TalkToolClient> mClientsById;
    List<TalkToolClient> mSelectedClients;
    WebSocketClientFactory mWSClientFactory;


    private static KeyStore getKeyStore() {
        if(KEYSTORE == null) {
            throw new RuntimeException("SSL security not initialized");
        }
        return KEYSTORE;
    }

    private static void initializeSsl() {
        try {
            // get the keystore
            KeyStore ks = KeyStore.getInstance("BKS");
            // load keys
            InputStream input = TalkToolContext.class.getClassLoader().getResourceAsStream("ssl_bks");
            try {
                ks.load(input, "password".toCharArray());
            } finally {
                input.close();
            }
            // configure HttpClient
            HttpClientWithKeystore.initializeSsl(ks);

            KEYSTORE = ks;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void configureSsl(WebSocketClientFactory wsClientFactory) {
        SslContextFactory sslcFactory = wsClientFactory.getSslContextFactory();
        sslcFactory.setTrustAll(false);
        sslcFactory.setKeyStore(getKeyStore());
        sslcFactory.setEnableCRLDP(false);
        sslcFactory.setEnableOCSP(false);
        sslcFactory.setSessionCachingEnabled(XoClientConfiguration.TLS_SESSION_CACHE_ENABLED);
        sslcFactory.setSslSessionCacheSize(XoClientConfiguration.TLS_SESSION_CACHE_SIZE);
        sslcFactory.setIncludeCipherSuites(XoClientConfiguration.TLS_CIPHERS);
        sslcFactory.setIncludeProtocols(XoClientConfiguration.TLS_PROTOCOLS);
    }

    public TalkToolContext(TalkTool app) {
        super(app);
        Console.info("- setting up TalkToolContext...");
        mApplication = app;
        mMapper = new ObjectMapper();
        mExecutor = Executors.newScheduledThreadPool(8);
        mClientIdCounter = new AtomicInteger(0);
        mClients = new Vector<TalkToolClient>();
        mClientsById = new Hashtable<Integer, TalkToolClient>();
        mSelectedClients = new Vector<TalkToolClient>();
        mWSClientFactory = new WebSocketClientFactory();
    }

    public void setupSsl() {
        Console.info("- setting up ssl...");
        initializeSsl();
        configureSsl(mWSClientFactory);
    }

    public void start() {
        try {
            Console.info("- starting WebsocketClientFactory...");
            mWSClientFactory.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TalkTool getApplication() {
        return mApplication;
    }

    public ScheduledExecutorService getExecutor() {
        return mExecutor;
    }

    public ObjectMapper getMapper() {
        return mMapper;
    }

    public WebSocketClientFactory getWSClientFactory() {
        return mWSClientFactory;
    }

    public List<TalkToolClient> getClients() {
        return new Vector<TalkToolClient>(mClients);
    }

    public List<TalkToolClient> getSelectedClients() {
        return new Vector<TalkToolClient>(mSelectedClients);
    }

    public void setSelectedClients(List<TalkToolClient> selectedClients) {
        this.mSelectedClients = new Vector<TalkToolClient>(selectedClients);
    }

    public void addClient(TalkToolClient client) throws SQLException {
        mClients.add(client);
        mClientsById.put(client.getId(), client);
        client.initialize();
    }

    public List<TalkToolClient> getClientsBySelectors(List<String> selectors) {
        ArrayList<TalkToolClient> clients = new ArrayList<TalkToolClient>(selectors.size());
        for (int i = 0; i < selectors.size(); i++) {
            String name = selectors.get(i);
            TalkToolClient client = getClientBySelector(name);
            clients.add(i, client);
        }
        return clients;
    }

    public TalkToolClient getClientBySelector(String selector) {
        TalkToolClient client = null;
        try {
            int id = Integer.parseInt(selector);
            client = getClientById(id);
        } catch (NumberFormatException e) {
        }
        if (client == null) {
            client = getClientByClientId(selector);
        }
        return client;
    }

    public TalkToolClient getClientById(int id) {
        return mClientsById.get(id);
    }

    public TalkToolClient getClientByClientId(String clientId) {
        for (TalkToolClient client : mClients) {
            String id = client.getClientId();
            if (id != null && id.equals(clientId)) {
                return client;
            }
        }
        return null;
    }

    public int generateId() {
        return mClientIdCounter.incrementAndGet();
    }

}
