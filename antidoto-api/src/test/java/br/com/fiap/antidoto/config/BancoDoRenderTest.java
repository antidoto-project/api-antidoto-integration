package br.com.fiap.antidoto.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** A DATABASE_URL do Render (postgresql://...) vira a conexão JDBC do Spring. */
class BancoDoRenderTest {

    @Test
    void urlInternaDoRenderSemPortaUsaA5432() {
        var conexao = BancoDoRender.paraJdbc("postgresql://antidoto:s3nh4@dpg-abc123-a/antidoto");

        assertThat(conexao.url()).isEqualTo("jdbc:postgresql://dpg-abc123-a:5432/antidoto");
        assertThat(conexao.usuario()).isEqualTo("antidoto");
        assertThat(conexao.senha()).isEqualTo("s3nh4");
    }

    @Test
    void urlExternaComPortaParametrosESenhaCodificada() {
        var conexao = BancoDoRender.paraJdbc(
                "postgres://antidoto:a%40b%3Ac@dpg-abc123-a.virginia-postgres.render.com:6543/antidoto?sslmode=require");

        assertThat(conexao.url())
                .isEqualTo("jdbc:postgresql://dpg-abc123-a.virginia-postgres.render.com:6543/antidoto?sslmode=require");
        assertThat(conexao.senha()).isEqualTo("a@b:c");
    }

    @Test
    void formatoErradoParaNaSubidaComMensagemClara() {
        assertThatThrownBy(() -> BancoDoRender.paraJdbc("mysql://x:y@host/banco"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("postgresql://");
    }

    @Test
    void comDatabaseUrlPreencheODatasource() {
        var ambiente = new StandardEnvironment();
        ambiente.getPropertySources().addFirst(new MockPropertySource()
                .withProperty("DATABASE_URL", "postgresql://u:p@host/banco"));

        new BancoDoRender().postProcessEnvironment(ambiente, new SpringApplication());

        assertThat(ambiente.getProperty("spring.datasource.url")).isEqualTo("jdbc:postgresql://host:5432/banco");
        assertThat(ambiente.getProperty("spring.datasource.username")).isEqualTo("u");
        assertThat(ambiente.getProperty("spring.datasource.password")).isEqualTo("p");
    }

    @Test
    void comDbUrlManualOuSemNadaNaoMexe() {
        var manual = new StandardEnvironment();
        manual.getPropertySources().addFirst(new MockPropertySource()
                .withProperty("DATABASE_URL", "postgresql://u:p@host/banco")
                .withProperty("DB_URL", "jdbc:postgresql://outro:5432/banco"));
        new BancoDoRender().postProcessEnvironment(manual, new SpringApplication());
        assertThat(manual.getProperty("spring.datasource.url")).isNull();

        var vazio = new StandardEnvironment();
        new BancoDoRender().postProcessEnvironment(vazio, new SpringApplication());
        assertThat(vazio.getPropertySources().contains("banco-do-render")).isFalse();
    }
}
