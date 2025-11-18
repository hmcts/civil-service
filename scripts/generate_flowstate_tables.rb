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

def load_mapping(path)
  YAML.safe_load(File.read(path))
end

def merge_whitelist(mapping, whitelist)
  mapping.each do |state, events|
    if state.start_with?('MAIN.')
      current_events = events || []
      mapping[state] = (current_events + whitelist).uniq.sort
    end
  end
  mapping
end

def write_diagram(target, contents)
  FileUtils.mkdir_p(File.dirname(target))
  File.write(target, contents)
end

if __FILE__ == $PROGRAM_NAME
  generate_general = true
  generate_spec = true
  include_whitelist = ARGV.include?('--inc-whitelist')

  if ARGV.include?('--general') && !ARGV.include?('--spec')
    generate_spec = false
  elsif ARGV.include?('--spec') && !ARGV.include?('--general')
    generate_general = false
  end

  whitelist = []
  if include_whitelist
    whitelist_path = File.join(CONFIG_DIR, 'allowed-whitelist-events.yml')
    if File.exist?(whitelist_path)
      whitelist = load_mapping(whitelist_path)
    else
      puts "Warning: Whitelist file not found at #{whitelist_path}"
    end
  end

  if generate_general
    mapping = load_mapping(File.join(CONFIG_DIR, 'allowed-unspec-events.yml'))
    mapping = merge_whitelist(mapping, whitelist) if include_whitelist && !whitelist.empty?
    write_diagram(File.join(DOCS_DIR, 'allowed-unspec-events.md'),
                  StateTable.new('allowed-unspec-events.yml', mapping).render)
  end

  if generate_spec
    mapping = load_mapping(File.join(CONFIG_DIR, 'allowed-spec-events.yml'))
    mapping = merge_whitelist(mapping, whitelist) if include_whitelist && !whitelist.empty?
    write_diagram(File.join(DOCS_DIR, 'allowed-spec-events.md'),
                  StateTable.new('allowed-spec-events.yml', mapping).render)
  end
end
