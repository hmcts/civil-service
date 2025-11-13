#!/usr/bin/env ruby
# frozen_string_literal: true

require 'yaml'
require 'fileutils'

ROOT = File.expand_path('..', __dir__)
CONFIG_DIR = File.join(ROOT, 'src', 'main', 'resources', 'config')
DOCS_DIR = File.join(ROOT, 'docs')

class StateTable
  def initialize(source_name, mapping)
    @source_name = source_name
    @mapping = mapping
  end

  def render
    lines = ["<!-- Auto-generated from #{@source_name}; edit the YAML instead -->", ""]
    lines << "## Table of contents"
    lines << ''
    @mapping.keys.sort.each do |state|
      lines << "- [#{state}](##{anchor(state)})"
    end
    lines << ''

    @mapping.sort.each do |state, events|
      lines << "## #{state}"
      lines << "<a id=\"#{anchor(state)}\"></a>"
      lines << ''
      if events && !events.empty?
        lines << '| Allowed event |'
        lines << '| --- |'
        events.each { |event| lines << "| `#{event}` |" }
      else
        lines << '_No events configured_'
      end
      lines << ''
    end

    lines.join("\n")
  end

  private

  def anchor(state)
    state.downcase.gsub(/[^a-z0-9]+/, '-').gsub(/^-|-$/, '')
  end
end

def load_mapping(path, key)
  data = YAML.safe_load(File.read(path))
  data.fetch('flow-state').fetch(key)
end

def write_diagram(target, contents)
  FileUtils.mkdir_p(File.dirname(target))
  File.write(target, contents)
end

if __FILE__ == $PROGRAM_NAME
  generate_general = true
  generate_spec = true

  if ARGV.include?('--general') && !ARGV.include?('--spec')
    generate_spec = false
  elsif ARGV.include?('--spec') && !ARGV.include?('--general')
    generate_general = false
  end

  if generate_general
    mapping = load_mapping(File.join(CONFIG_DIR, 'flowstate-allowed-events.yml'), 'allowed-events')
    write_diagram(File.join(DOCS_DIR, 'flowstate_allowed_events.md'),
                  StateTable.new('flowstate-allowed-events.yml', mapping).render)
  end

  if generate_spec
    mapping = load_mapping(File.join(CONFIG_DIR, 'flowstate-allowed-spec-events.yml'), 'allowed-events-spec')
    write_diagram(File.join(DOCS_DIR, 'flowstate_allowed_spec_events.md'),
                  StateTable.new('flowstate-allowed-spec-events.yml', mapping).render)
  end
end
