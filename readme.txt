Configuração do Ambiente + Spring Boot + Maven + Git

- Instalar VS Code
- Instalar snipet maven
- Instalar snipet Java pack for VS
- Instalar Language Support for Java By red hat
- Isso ajuda também: https://github.com/redhat-developer/vscode-java/issues/100#issuecomment-299026100
- Criar /sistema
- Criar repositório git no bitbucket
- cd /sistema, git clone [url do repositório]
- cd websys (diretório do repositório)
- No VS Code, abrir o diretório /sistema/sistema
- Botão direito, generate from maven archetype, selecina o diretório current (/sistema/sistema, depois maven-archteype-quickstart
- Ajustar pom.xml, etc
- cd /sistema/sistema/websys e mvn spring-boot:run

URLs de login:
http://localhost:5500

Database: erp



-- prever contas a pagar/receber fixas
- pega a data de hoje
- adiciona 10 dias (ou a gosto)
- pega o escopo (mes/ano) dessa data futura
- verifica as contas que se repetem nesse escopo
- verifica se a diferença de dias entre to_date(concat(expiration_day, '/07/2018'), 'dd/MM/YYYY')) - now() é menor que 10
e se foi paga ou não




SELECT * FROM (
    SELECT 
    -- cria a data com base no dia de vencimento + escopo
    date_trunc('day', to_date(concat(expiration_day, '/07/2018'), 'dd/MM/YYYY')) as exp_date,
    -- data de referencia, vai ser sempre NOW() apenas
    NOW() as reference_date,
    *
    FROM public.wfs_fixed_account
    -- pega só o que repete no mês do escopo, pode ser feito via programação
    -- WHERE repeatjul=TRUE
) q1
-- onde intervalo entre hoje (reference_date) e data de vencimento (exp_date) for 
-- menor ou igual à 15 dias
WHERE date_part('day', age(exp_date, reference_date )) <= 0
AND exp_date >= enabled_date
AND (exp_date <= disabled_date OR disabled_date IS NULL)

