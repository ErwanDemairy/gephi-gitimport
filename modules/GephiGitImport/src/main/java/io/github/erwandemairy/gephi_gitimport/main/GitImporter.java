package io.github.erwandemairy.gephi_gitimport.main;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;
import java.util.logging.Logger;

@ServiceProvider(service = Generator.class)
public class GitImporter implements Generator {
    protected File repoDir;
    protected boolean cancel = false;
    protected ProgressTicket progress;

    private static final Logger LOGGER = Logger.getLogger(GitImporter.class.getName());
    private ContainerLoader container;
//    private Report report;
//
    @Override
    public void generate(ContainerLoader containerLoader) {
        this.container = containerLoader;

//        File repoDir = new File("/Users/edemairy/tmp/update-semanticwebimport");
        try (Repository repository = Git.open(repoDir).getRepository()) {
            String branch = repository.getBranch();
            LOGGER.info("Current branch: " + branch);

            ObjectId head = repository.resolve("HEAD");
            System.out.println(MessageFormat.format("Head {0}", head.getName()));
            try (RevWalk revWalk = new RevWalk(repository)) {
                revWalk.markStart(revWalk.parseCommit(repository.resolve("HEAD")));

                for (RevCommit commit : revWalk) {
                    LOGGER.fine("Commit: " + commit.getId().getName());
                    LOGGER.fine("Author: " + commit.getAuthorIdent().getName());
                    LOGGER.fine("Date: " + commit.getAuthorIdent().getWhen());
                    LOGGER.fine("Message: " + commit.getFullMessage());
                    LOGGER.fine("---------------------------------------------------");

                    NodeDraft commitNode = container.factory().newNodeDraft(commit.getName());
                    commitNode.setValue("Type", "commit");
                    container.addNode(commitNode);

                    NodeDraft authorNode = container.factory().newNodeDraft(commit.getAuthorIdent().getName());
                    authorNode.setValue("Type", "author");
                    container.addNode(authorNode);

                    EdgeDraft authorEdge = container.factory().newEdgeDraft();
                    authorEdge.setSource(commitNode);
                    authorEdge.setTarget(authorNode);
                    authorEdge.setLabel("author");
                    container.addEdge(authorEdge);

                    NodeDraft dateNode = container.factory().newNodeDraft(commit.getCommitterIdent().getWhen().toString());
                    dateNode.setValue("Type", "date");
                    container.addNode(dateNode);

                    EdgeDraft dateEdge = container.factory().newEdgeDraft();
                    dateEdge.setSource(commitNode);
                    dateEdge.setTarget(dateNode);
                    dateEdge.setLabel("date");
                    container.addEdge(dateEdge);

                    NodeDraft messageNode = container.factory().newNodeDraft(commit.getFullMessage());
                    messageNode.setValue("Type", "message");
                    container.addNode(messageNode);

                    EdgeDraft messageEdge = container.factory().newEdgeDraft();
                    messageEdge.setSource(commitNode);
                    messageEdge.setTarget(messageNode);
                    messageEdge.setLabel("message");
                    container.addEdge(messageEdge);

                    for (var parent : commit.getParents()) {
                        NodeDraft parentNode = container.factory().newNodeDraft(parent.getName());
                        parentNode.setValue("Type", "commit");
                        EdgeDraft parentEdge = container.factory().newEdgeDraft();
                        parentEdge.setLabel("parent");
                        parentEdge.setSource(commitNode);
                        parentEdge.setTarget(parentNode);

                        container.addNode(parentNode);
                        container.addEdge(parentEdge);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        // Exemple de données CSV (vous pouvez remplacer ceci par un Reader depuis un fichier réel)
//        String csvData = "Id,Label\n1,Node1\n2,Node2\n1,2";
//        try (BufferedReader reader = new BufferedReader(new java.io.StringReader(csvData))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] tokens = line.split(",");
//                if (tokens.length == 2) { // Ajouter des nœuds
//                    NodeDraft node = container.factory().newNodeDraft(tokens[0]);
//                    node.setLabel(tokens[1]);
//                    container.addNode(node);
//                } else if (tokens.length == 3) { // Ajouter des arcs
//                    EdgeDraft edge = container.factory().newEdgeDraft();
//                    edge.setLabel(currentEdge.getLabel());
//                    edge.setStokens[0], tokens[1]);
//                    container.addEdge(edge);
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.severe("Erreur d'importation : " + e.getMessage());
//        }
    }

    @Override
    public String getName() {
        return "Git Importer";
    }

    @Override
    public GeneratorUI getUI() {
        return new GitImporterUI();
    }

    @Override
    public boolean cancel() {
        cancel =true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    public void setDirectory(String directory) {
        this.repoDir = new File(directory);
    }
}