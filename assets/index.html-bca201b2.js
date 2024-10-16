import{_ as l,r as o,o as r,c as u,a as n,b as t,d as a,w as s,e as d}from"./app-05649504.js";const m={},k=d(`<h2 id="example" tabindex="-1"><a class="header-anchor" href="#example" aria-hidden="true">#</a> Example</h2><p>Add dependency to your project:</p><div class="language-xml line-numbers-mode" data-ext="xml"><pre class="language-xml"><code><span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>dependency</span><span class="token punctuation">&gt;</span></span>
  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>groupId</span><span class="token punctuation">&gt;</span></span>us.abstracta.jmeter<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>groupId</span><span class="token punctuation">&gt;</span></span>
  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>artifactId</span><span class="token punctuation">&gt;</span></span>jmeter-java-dsl<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>artifactId</span><span class="token punctuation">&gt;</span></span>
  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>version</span><span class="token punctuation">&gt;</span></span>1.29.1<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>version</span><span class="token punctuation">&gt;</span></span>
  <span class="token tag"><span class="token tag"><span class="token punctuation">&lt;</span>scope</span><span class="token punctuation">&gt;</span></span>test<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>scope</span><span class="token punctuation">&gt;</span></span>
<span class="token tag"><span class="token tag"><span class="token punctuation">&lt;/</span>dependency</span><span class="token punctuation">&gt;</span></span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div><p>Create performance test:</p><div class="language-java line-numbers-mode" data-ext="java"><pre class="language-java"><code><span class="token keyword">import</span> <span class="token keyword">static</span> <span class="token import static"><span class="token namespace">org<span class="token punctuation">.</span>assertj<span class="token punctuation">.</span>core<span class="token punctuation">.</span>api<span class="token punctuation">.</span></span><span class="token class-name">Assertions</span><span class="token punctuation">.</span><span class="token static">assertThat</span></span><span class="token punctuation">;</span>
<span class="token keyword">import</span> <span class="token keyword">static</span> <span class="token import static"><span class="token namespace">us<span class="token punctuation">.</span>abstracta<span class="token punctuation">.</span>jmeter<span class="token punctuation">.</span>javadsl<span class="token punctuation">.</span></span><span class="token class-name">JmeterDsl</span><span class="token punctuation">.</span><span class="token operator">*</span></span><span class="token punctuation">;</span>

<span class="token keyword">import</span> <span class="token import"><span class="token namespace">java<span class="token punctuation">.</span>io<span class="token punctuation">.</span></span><span class="token class-name">IOException</span></span><span class="token punctuation">;</span>
<span class="token keyword">import</span> <span class="token import"><span class="token namespace">java<span class="token punctuation">.</span>time<span class="token punctuation">.</span></span><span class="token class-name">Duration</span></span><span class="token punctuation">;</span>
<span class="token keyword">import</span> <span class="token import"><span class="token namespace">org<span class="token punctuation">.</span>junit<span class="token punctuation">.</span>jupiter<span class="token punctuation">.</span>api<span class="token punctuation">.</span></span><span class="token class-name">Test</span></span><span class="token punctuation">;</span>
<span class="token keyword">import</span> <span class="token import"><span class="token namespace">us<span class="token punctuation">.</span>abstracta<span class="token punctuation">.</span>jmeter<span class="token punctuation">.</span>javadsl<span class="token punctuation">.</span>core<span class="token punctuation">.</span></span><span class="token class-name">TestPlanStats</span></span><span class="token punctuation">;</span>

<span class="token keyword">public</span> <span class="token keyword">class</span> <span class="token class-name">PerformanceTest</span> <span class="token punctuation">{</span>

  <span class="token annotation punctuation">@Test</span>
  <span class="token keyword">public</span> <span class="token keyword">void</span> <span class="token function">testPerformance</span><span class="token punctuation">(</span><span class="token punctuation">)</span> <span class="token keyword">throws</span> <span class="token class-name">IOException</span> <span class="token punctuation">{</span>
    <span class="token class-name">TestPlanStats</span> stats <span class="token operator">=</span> <span class="token function">testPlan</span><span class="token punctuation">(</span>
            <span class="token function">threadGroup</span><span class="token punctuation">(</span><span class="token number">2</span><span class="token punctuation">,</span> <span class="token number">10</span><span class="token punctuation">,</span>
                    <span class="token function">httpSampler</span><span class="token punctuation">(</span><span class="token string">&quot;http://my.service&quot;</span><span class="token punctuation">)</span>
            <span class="token punctuation">)</span>
    <span class="token punctuation">)</span><span class="token punctuation">.</span><span class="token function">run</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
    <span class="token function">assertThat</span><span class="token punctuation">(</span>stats<span class="token punctuation">.</span><span class="token function">overall</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">.</span><span class="token function">sampleTimePercentile99</span><span class="token punctuation">(</span><span class="token punctuation">)</span><span class="token punctuation">)</span><span class="token punctuation">.</span><span class="token function">isLessThan</span><span class="token punctuation">(</span><span class="token class-name">Duration</span><span class="token punctuation">.</span><span class="token function">ofSeconds</span><span class="token punctuation">(</span><span class="token number">5</span><span class="token punctuation">)</span><span class="token punctuation">)</span><span class="token punctuation">;</span>
  <span class="token punctuation">}</span>

<span class="token punctuation">}</span>
</code></pre><div class="line-numbers" aria-hidden="true"><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div><div class="line-number"></div></div></div>`,5),h={href:"https://github.com/abstracta/jmeter-java-dsl-sample",target:"_blank",rel:"noopener noreferrer"},v=n("h2",{id:"hear-it-from-our-community",tabindex:"-1"},[n("a",{class:"header-anchor",href:"#hear-it-from-our-community","aria-hidden":"true"},"#"),t(" Hear It From Our Community")],-1),f=n("p",null,"It's a great new functionality. If you don't know about you definitely should check it out.",-1),g=n("p",null,'JMeterDSL is really easy to pick up for a team with some familiar with Java and JMeter. My team was able to learn and start using it within a day! We love it that we can create reusable and extensible "script" components to assemble our performance tests with great ease. The ability to branch and merge (no more XML!) is a breath of fresh air. The ability to run the tests locally or remotely (Azure, in our case) is exactly what we are looking for! Thank you for creating this!',-1),b=n("p",null,"java-jmeter-dsl is a real breath of fresh air for us. This tool gave us a new vision of how tests can be built, how to integrate them into processes and how to automate them. And we can do all this on the basis of the well-known popular and proven Apache JMeter engine. java-jmeter-dsl gives your JMeter-based tests maintainability and reusability at the level of any code solution, which is actually a must for large teams or teams with a large number of tests",-1),w=n("p",null,"I strongly encourage you to check this very promising project and give it a start on GitHub if you like it",-1),y=n("p",null,"As more of a java developer and less of a jmeter engineer, I have always wished this",-1),_=n("p",null,"Demo of the @AbstractaUS Jmeter DSL with @rabelenda is soooooo cooooool",-1),j=n("p",null,"I am an early bird user, it is so elegant, extendable, and flexible even from the first commits, thank you so much, Roger and Team. Java DSL is one love for sure! Inspired me to new ideas, and allowed me to verify the correctness of my theories - Java DSL rock this Jmeter party!!!",-1),I=n("p",null,"La estuve viendo un poco (JMeter DSL), haciendo lo básico! Pero es súper recomendable! Es JMeter pero en tu repo amigo! con Java! 👨🏻‍💻🚀",-1),A=n("p",null,"JMeter DSL brings JMeter to the frontline of technological ingenuity. It solves most of the problems traditionally associated with JMeter such as lack of dependency management, difficulties with source control integration and difficulties to extend.",-1),x={href:"https://pymeter.readthedocs.io/en/latest/",target:"_blank",rel:"noopener noreferrer"},S=n("p",null,"java-jmeter-dsl is a great tool because it can help you reuse your REST-assured tests in order to create some performance test cases. It's very useful because you can generate JTL files to create JMeter reports and even use InfluxDB and Grafana to generate very nice graphics.",-1),J=n("p",null,"It will be the new direction for JMeter for sure",-1),M=n("p",null,"I just started exploring JMeter DSL. It is pretty cool. I will post an article and video soon. #jmeter",-1),T=n("p",null,"Write concise, readable code that's easier to maintain and update using JMeter DSL from Abstracta.",-1),P=n("p",null,"I tried using JMeter-Java-DSL. It's awesome, I can combine Selenium & JMeter library code in single script from comfort of my IDE.",-1),L=n("p",null,"Probé JMeter DSL para Performance Testing y en unas pocas líneas de código puedes ejecutarlo. Realmente muy útil.",-1),D=n("p",null,"JMeter Java DSL is a really useful avatar of the load testing tool. This is specially good for CI/CD projects where developers should be using some form of performance evaluation to validate a good build.",-1),E=n("p",null,"I was looking for simplicity of test plan implementation, reusability of components, assertion on metrics, html report per test, ease of debugging and maintenance, great support and documentation with tons of examples - and I found them here. JMeter DSL covers JMeter's missing functionality and does it great.",-1),C=n("p",null,"I always wished to have easy DSL for jmeter when other tools were supporting such as gatling. I was delighted when jmeter dsl was introduced and tried in my work project. It works so good in simple projects. Establishing CI pipeline is also smooth and easy now in organisation. Thank you for your valuable effort.",-1),B={style:{margin:"10px"}};function G(O,R){const i=o("ExternalLinkIcon"),e=o("testimonial"),p=o("carousel"),c=o("AutoLink");return r(),u("div",null,[k,n("p",null,[t("You can use "),n("a",h,[t("this project"),a(i)]),t(" as a starting point.")]),v,a(p,null,{default:s(()=>[a(e,{item:{source:"https://www.linkedin.com/pulse/oop-automation-testing-jmeter-java-dsl-more-joe-colantonio ",name:" Joe Colantonio ",position:" Founder @ TestGuild"}},{default:s(()=>[f]),_:1},8,["item"]),a(e,{item:{source:"https://github.com/abstracta/jmeter-java-dsl/issues/201#issuecomment-1638715839 ",name:" Mike Liu ",position:" Senior Software Engineering Manager @ MGM Resorts International"}},{default:s(()=>[g]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/kirill-yurkov-31b42ba1 ",name:" Kirill Yurkov ",position:" Head of Observability & Reliability @ Самокат"}},{default:s(()=>[b]),_:1},8,["item"]),a(e,{item:{source:"https://octoperf.com/blog/2022/06/13/jmeter-test-as-code ",name:" Gérald Pereira ",position:" CTO @ OctoPerf"}},{default:s(()=>[w]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/posts/krmahadevan_github-abstractajmeter-java-dsl-simple-activity-6848882314567647232-3VG-?utm_source=share&utm_medium=member_desktop ",name:" Krishnan Mahadevan ",position:""}},{default:s(()=>[y]),_:1},8,["item"]),a(e,{item:{source:"https://twitter.com/PerfBytes/status/1540433983237939200 ",name:" PerfBytes ",position:""}},{default:s(()=>[_]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/isakovoleksii/ ",name:" Oleksii Isakov ",position:" Software Development Engineer in Test & Performance Analyst"}},{default:s(()=>[j]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/posts/marto-vasconcelo-1910014b_jmeter-scripting-la-pieza-faltante-roger-activity-7031768122055364608-bqGp ",name:" Marto Vasconcelo ",position:" Analista de sistemas de TI @ Cencosud S.A."}},{default:s(()=>[I]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/feed/update/urn:li:ugcPost:6973987279207882752?commentUrn=urn%3Ali%3Acomment%3A%28ugcPost%3A6973987279207882752%2C6973992977908027392%29&dashCommentUrn=urn%3Ali%3Afsd_comment%3A%286973992977908027392%2Curn%3Ali%3AugcPost%3A6973987279207882752%29 ",name:" Eldad Uzman ",position:" Automation Architect"}},{default:s(()=>[A,n("p",null,[t("That, with addition to the highly engaging attitude of Abstracta, has inspired me into extending the project further by introducing "),n("a",x,[t("PYmeter"),a(i)]),t(", a python DSL package for JMeter based on jmeter-java-dsl.")])]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/tamasbojte/ ",name:" Tamás Bőjte ",position:" Senior Automation Engineer"}},{default:s(()=>[S]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/uddipan-halder-b5241b84/ ",name:" Uddipan Halder ",position:" Lead Performance Test Engineer"}},{default:s(()=>[J]),_:1},8,["item"]),a(e,{item:{source:"https://twitter.com/QAInsights/status/1520577180744523776 ",name:" NaveenKumar Namachivayam ",position:" Performance Engineer"}},{default:s(()=>[M]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/posts/microsoft-developers_write-concise-readable-code-thats-easier-activity-7079873611678814208-qXl7 ",name:" Microsoft Developer ",position:""}},{default:s(()=>[T]),_:1},8,["item"]),a(e,{item:{source:"https://twitter.com/abhaybharti/status/1552149409802354688 ",name:" Abhay Bharti ",position:" Principal Engineer"}},{default:s(()=>[P]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/posts/pablo-herrera-ec_java-maven-web-activity-6982698450366648320-3rvZ ",name:" Pablo Herrera ",position:" QA Automation Engineer"}},{default:s(()=>[L]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/posts/vishalendupandey_releases-abstractajmeter-java-dsl-activity-7016121555520745472-nv9Y ",name:" Vishalendu Pandey ",position:" Performance Architect"}},{default:s(()=>[D]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/rados%C5%82aw-siatka-62a5a66/ ",name:" Radoslaw Siatka ",position:" Test Lead @ GFT Poland"}},{default:s(()=>[E]),_:1},8,["item"]),a(e,{item:{source:"https://www.linkedin.com/in/premraj-murugaraj/ ",name:" Premraj Murugaraj ",position:" Senior Engineer, Testing @ Singtel"}},{default:s(()=>[C]),_:1},8,["item"])]),_:1}),n("div",B,[a(c,{item:{link:"https://forms.gle/h2A7zbHKRiSvCqBd7",text:"Share your testimonial",icon:"fa-solid fa-bullhorn"}},null,8,["item"])])])}const H=l(m,[["render",G],["__file","index.html.vue"]]);export{H as default};
