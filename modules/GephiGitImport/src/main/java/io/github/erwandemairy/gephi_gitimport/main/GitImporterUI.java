package io.github.erwandemairy.gephi_gitimport.main;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;

import javax.swing.*;
import java.util.logging.Logger;

public class GitImporterUI implements GeneratorUI {
    Logger LOGGER  = Logger.getLogger(GitImporterUI.class.getName());
    protected  GitImporter importer;
    JTextField gitRepository;
    @Override
    public JPanel getPanel() {
        LOGGER.info("getPanel()");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Git Importer"));
        panel.add(Box.createVerticalStrut(5));
        gitRepository = new JTextField();
        panel.add(gitRepository);
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        LOGGER.info("Setting up GitImporter");
        importer = (GitImporter) generator;
    }

    @Override
    public void unsetup() {
        LOGGER.info("un - Setting up GitImporter");
        importer.setDirectory(gitRepository.getText());
    }
}
