package br.com.fiap.antidoto.config;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * No Render, o render.yaml entrega o banco em DATABASE_URL no formato
 * postgresql://usuario:senha@host:porta/banco (o "connectionString" do Postgres).
 * O driver JDBC não aceita esse formato, então aqui ele vira spring.datasource.url,
 * username e password, antes de o Spring montar a conexão.
 * Se DB_URL estiver definido (configuração manual), ele manda e nada muda aqui.
 * Registrado em META-INF/spring.factories.
 */
public class BancoDoRender implements EnvironmentPostProcessor {

    static final String VARIAVEL_DO_RENDER = "DATABASE_URL";
    private static final int PORTA_PADRAO = 5432;

    record ConexaoJdbc(String url, String usuario, String senha) {
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment ambiente, SpringApplication aplicacao) {
        String urlDoRender = ambiente.getProperty(VARIAVEL_DO_RENDER);
        if (urlDoRender == null || urlDoRender.isBlank() || ambiente.containsProperty("DB_URL")) {
            return;
        }
        ConexaoJdbc conexao = paraJdbc(urlDoRender);
        ambiente.getPropertySources().addFirst(new MapPropertySource("banco-do-render", Map.of(
                "spring.datasource.url", conexao.url(),
                "spring.datasource.username", conexao.usuario(),
                "spring.datasource.password", conexao.senha()
        )));
    }

    /** postgresql://usuario:senha@host[:porta]/banco[?parametros] → jdbc:postgresql://host:porta/banco[?parametros]. */
    static ConexaoJdbc paraJdbc(String urlDoRender) {
        URI uri = URI.create(urlDoRender.trim());
        if (!"postgresql".equals(uri.getScheme()) && !"postgres".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL precisa começar com postgresql:// (o connectionString do Render)");
        }
        String[] credenciais = uri.getRawUserInfo() == null ? new String[0] : uri.getRawUserInfo().split(":", 2);
        String usuario = credenciais.length > 0 ? decodificar(credenciais[0]) : "";
        String senha = credenciais.length > 1 ? decodificar(credenciais[1]) : "";
        int porta = uri.getPort() == -1 ? PORTA_PADRAO : uri.getPort();
        String banco = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
        String parametros = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();

        return new ConexaoJdbc(
                "jdbc:postgresql://" + uri.getHost() + ":" + porta + "/" + banco + parametros, usuario, senha);
    }

    private static String decodificar(String parte) {
        return URLDecoder.decode(parte, StandardCharsets.UTF_8);
    }
}
